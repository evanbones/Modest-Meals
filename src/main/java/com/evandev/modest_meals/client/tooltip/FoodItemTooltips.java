package com.evandev.modest_meals.client.tooltip;

import com.evandev.modest_meals.client.ModSprites;
import com.evandev.modest_meals.client.hud.StaminaSprites;
import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightCompat;
import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightSoupEffects;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.config.TooltipVisibility;
import com.evandev.modest_meals.food.EdibleBlockFoods;
import com.evandev.modest_meals.food.FoodValues;
import com.evandev.modest_meals.food.ingredient.IngredientProfile;
import com.evandev.modest_meals.food.ingredient.IngredientProfileManager;
import com.evandev.modest_meals.food.ingredient.MealEffectManager;
import com.evandev.modest_meals.stamina.StaminaData;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.impl.EffectGrantTrait;
import com.evandev.modest_meals.trait.impl.HealthAdditionTrait;
import com.evandev.modest_meals.trait.impl.StaminaAdditionTrait;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.*;

public class FoodItemTooltips {
    private static final String CONFIG_PREFIX = "gui.modest_meals.regeneration_tooltip.";

    private static final IconRowTooltip.Sprites HEART_SPRITES = new IconRowTooltip.Sprites(
            Gui.HeartType.CONTAINER.getSprite(false, false, false),
            Gui.HeartType.NORMAL.getSprite(false, false, false),
            Gui.HeartType.NORMAL.getSprite(false, true, false)
    );

    // TODO: should this be configurable?
    private static final int COMPACT_POINTS_THRESHOLD = 12;

    private static IconRowTooltip.Sprites staminaSprites() {
        IconRowTooltip.Icon half = StaminaSprites.isOtherHalfAvailable()
                ? IconRowTooltip.Icon.texture(StaminaSprites.STAMINA_LEVEL_OTHER_HALF)
                : IconRowTooltip.Icon.sprite(ModSprites.STAMINA_LEVEL_HALF);
        return new IconRowTooltip.Sprites(
                IconRowTooltip.Icon.sprite(ModSprites.STAMINA_EMPTY),
                IconRowTooltip.Icon.sprite(ModSprites.STAMINA_LEVEL),
                half
        );
    }

    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        List<Component> lines = event.getToolTip();
        float tickRate = event.getContext().tickRate();

        List<FoodProperties.PossibleEffect> effects = foodEffects(stack);
        if (FoodValues.isFood(stack)) {
            removePreExistingFoodEffects(lines, effects);
        }

        if (ModConfig.get().showFoodItemTooltips) {
            addStatRows(lines, stack);
            addDigestionRateLine(lines, stack);
        }

        addEffectsAndTraits(lines, stack, effects, tickRate);
        addIngredientHint(lines, stack);
    }

    /**
     * On a raw ingredient, show the effect it would contribute to a meal.
     */
    private static void addIngredientHint(List<Component> lines, ItemStack stack) {
        if (!isVisible(ModConfig.get().ingredientTooltip)
                || stack.has(ModDataComponents.MEAL_CONTENTS.get())
                || !IngredientProfileManager.isIngredient(stack)) {
            return;
        }

        IngredientProfileManager.authored(stack)
                .filter(IngredientProfile::hasEffect)
                .flatMap(profile -> profile.effect().flatMap(MealEffectManager::get))
                .ifPresent(effect -> {
                    lines.add(Component.translatable("modest_meals.trait.header.ingredient")
                            .withStyle(ChatFormatting.GRAY));
                    lines.add(indent(effect.displayName().copy().withStyle(ChatFormatting.DARK_GRAY)));
                });
    }

    private static boolean isVisible(TooltipVisibility visibility) {
        return switch (visibility) {
            case VISIBLE -> true;
            case HIDDEN -> false;
            case SHIFT -> Screen.hasShiftDown();
        };
    }

    private static Component indent(Component line) {
        return Component.literal(" ").append(line);
    }

    private static void addStatRows(List<Component> lines, ItemStack stack) {
        int halfHearts = Mth.ceil(FoodValues.healthPoints(stack));
        int halfStamina = 0;
        if (ModConfig.get().staminaSprint) {
            float levels = FoodValues.staminaLevels(stack);
            if (levels > 0.0F) {
                halfStamina = Math.min(Mth.ceil(levels), staminaMaxLevel());
            }
        }

        if (halfHearts > 0 && halfStamina > 0) {
            if (halfHearts + halfStamina <= COMPACT_POINTS_THRESHOLD) {
                lines.add(new IconRowTooltip.Marker(
                        new IconRowTooltip.Entry(HEART_SPRITES, halfHearts),
                        new IconRowTooltip.Entry(staminaSprites(), halfStamina)
                ));
            } else {
                lines.add(new IconRowTooltip.Marker(HEART_SPRITES, halfHearts));
                lines.add(new IconRowTooltip.Marker(staminaSprites(), halfStamina));
            }
        } else if (halfHearts > 0) {
            lines.add(new IconRowTooltip.Marker(HEART_SPRITES, halfHearts));
        } else if (halfStamina > 0) {
            lines.add(new IconRowTooltip.Marker(staminaSprites(), halfStamina));
        }
    }

    private static int staminaMaxLevel() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return StaminaData.MAX_STAMINA_LEVEL;
        }
        return StaminaHelper.get(player).getMaxLevel();
    }

    private static void addDigestionRateLine(List<Component> lines, ItemStack stack) {
        if (!ModConfig.get().showFoodRegenerationTooltips || !ModConfig.get().gradualHealthRegeneration) {
            return;
        }
        float halfHearts = FoodValues.healthPoints(stack);
        int digestTicks = FoodValues.healthDigestTicks(stack);
        if (halfHearts <= 0.0F || digestTicks <= 0) {
            return;
        }

        float secondsPerHeart = digestTicks / halfHearts / 20.0F;
        String rate;
        ChatFormatting formatting;

        if (secondsPerHeart <= 0.5F) {
            rate = CONFIG_PREFIX + "super_fast";
            formatting = ChatFormatting.DARK_PURPLE;
        } else if (secondsPerHeart <= 0.8F) {
            rate = CONFIG_PREFIX + "very_fast";
            formatting = ChatFormatting.DARK_GREEN;
        } else if (secondsPerHeart <= 1.6F) {
            rate = CONFIG_PREFIX + "fast";
            formatting = ChatFormatting.GREEN;
        } else if (secondsPerHeart <= 2.5F) {
            rate = CONFIG_PREFIX + "slow";
            formatting = ChatFormatting.RED;
        } else {
            rate = CONFIG_PREFIX + "very_slow";
            formatting = ChatFormatting.DARK_RED;
        }

        lines.add(
                Component.translatable(CONFIG_PREFIX + "template", Component.translatable(rate))
                        .setStyle(Style.EMPTY.withColor(formatting.getColor()))
        );
    }

    public static boolean hasTooltipLine(FoodTrait trait) {
        return !(trait instanceof HealthAdditionTrait || trait instanceof StaminaAdditionTrait);
    }

    /**
     * Every effect eating this stack applies, including the ones other mods grant outside its food component.
     */
    private static List<FoodProperties.PossibleEffect> foodEffects(ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) {
            food = EdibleBlockFoods.getFoodProperties(stack.getItem()).orElse(null);
        }
        List<FoodProperties.PossibleEffect> effects = food == null ? List.of() : food.effects();

        if (FarmersDelightCompat.isLoaded()) {
            List<FoodProperties.PossibleEffect> soupEffects = FarmersDelightSoupEffects.get(stack);
            if (!soupEffects.isEmpty()) {
                List<FoodProperties.PossibleEffect> combined = new ArrayList<>(effects);
                combined.addAll(soupEffects);
                return combined;
            }
        }
        return effects;
    }

    private static void addEffectsAndTraits(List<Component> lines, ItemStack stack,
                                            List<FoodProperties.PossibleEffect> effects, float tickRate) {
        if (!FoodValues.isFood(stack) || !isVisible(ModConfig.get().whenEatenTooltip)) {
            return;
        }

        List<Component> effectLines = new ArrayList<>();
        List<Component> traitLines = new ArrayList<>();

        List<FoodProperties.PossibleEffect> shownEffects = new ArrayList<>(effects);
        List<FoodTrait> traits = new ArrayList<>();
        double valMult = ModConfig.get().traitGlobalValueMultiplier;
        double durMult = ModConfig.get().traitGlobalDurationMultiplier;

        if (ModConfig.get().showFoodTraitTooltips) {
            for (FoodTrait trait : FoodValues.effectiveTraits(stack)) {
                if (hasTooltipLine(trait)) {
                    traits.add(trait);
                }
            }
        }

        dropRedundantEffectGrants(shownEffects, traits, durMult);

        if (!shownEffects.isEmpty()) {
            collectFoodEffectTooltips(shownEffects, tickRate, effectLines);
        }

        for (FoodTrait trait : traits) {
            traitLines.add(trait.getTooltipComponent(valMult, durMult, tickRate));
        }

        if (!effectLines.isEmpty() || !traitLines.isEmpty()) {
            boolean isDrink = stack.getUseAnimation() == UseAnim.DRINK;
            lines.add(Component.translatable(isDrink ? "potion.whenDrank" : "modest_meals.trait.header.consumed")
                    .withStyle(ChatFormatting.GRAY));
            effectLines.forEach(line -> lines.add(indent(line)));
            traitLines.forEach(line -> lines.add(indent(line)));
        }
    }

    private static void dropRedundantEffectGrants(List<FoodProperties.PossibleEffect> effects,
                                                  List<FoodTrait> traits, double durationMultiplier) {
        for (Iterator<FoodTrait> it = traits.iterator(); it.hasNext(); ) {
            if (!(it.next() instanceof EffectGrantTrait grant)) {
                continue;
            }
            long grantDuration = (long) (grant.duration() * durationMultiplier);
            for (int i = 0; i < effects.size(); i++) {
                FoodProperties.PossibleEffect entry = effects.get(i);
                MobEffectInstance instance = entry.effect();
                if (entry.probability() < 1.0F
                        || !instance.getEffect().equals(grant.effect())
                        || instance.getAmplifier() != grant.amplifier()) {
                    continue;
                }
                if (grantDuration > instance.getDuration()) {
                    effects.remove(i);
                } else {
                    it.remove();
                }
                break;
            }
        }
    }

    private static void removePreExistingFoodEffects(List<Component> lines,
                                                     List<FoodProperties.PossibleEffect> effects) {
        Set<String> effectKeys = new HashSet<>();
        for (FoodProperties.PossibleEffect pe : effects) {
            effectKeys.add(pe.effect().getDescriptionId());
        }
        effectKeys.add("effect.farmersdelight.nourishment");
        effectKeys.add("effect.modest_meals.stamina_nourishment");
        effectKeys.add("effect.farmersdelight.comfort");

        boolean removingAttributes = false;
        for (int i = 1; i < lines.size(); ) {
            Component c = lines.get(i);
            if (isEffectTooltipLine(c, effectKeys)) {
                lines.remove(i);
                removingAttributes = true;
            } else if (removingAttributes && (c.getString().isEmpty() || isPotionAttributeLine(c))) {
                lines.remove(i);
            } else {
                removingAttributes = false;
                i++;
            }
        }
    }

    private static boolean isPotionAttributeLine(Component c) {
        if (c.getContents() instanceof TranslatableContents trans) {
            String key = trans.getKey();
            return "potion.whenDrank".equals(key) || "modest_meals.food.whenEaten".equals(key) || key.startsWith("attribute.modifier.");
        }
        return false;
    }

    private static void collectFoodEffectTooltips(
            List<FoodProperties.PossibleEffect> effects,
            float tickRate,
            List<Component> effectLines
    ) {
        for (FoodProperties.PossibleEffect entry : effects) {
            MobEffectInstance instance = entry.effect();
            MutableComponent mutableComponent = Component.translatable(instance.getDescriptionId());
            Holder<MobEffect> holder = instance.getEffect();

            if (instance.getAmplifier() > 0) {
                mutableComponent = Component.translatable(
                        "potion.withAmplifier",
                        mutableComponent,
                        Component.translatable("potion.potency." + instance.getAmplifier())
                );
            }

            if (!instance.endsWithin(20)) {
                mutableComponent = Component.translatable(
                        "potion.withDuration",
                        mutableComponent,
                        MobEffectUtil.formatDuration(instance, 1.0F, tickRate)
                );
            }

            if (entry.probability() < 1.0F) {
                mutableComponent = Component.translatable(
                        "modest_meals.food.withChance",
                        mutableComponent,
                        Math.round(entry.probability() * 100)
                );
            }

            effectLines.add(mutableComponent.withStyle(holder.value().getCategory().getTooltipFormatting()));
        }
    }

    private static boolean isEffectTooltipLine(Component c, Set<String> effectKeys) {
        if (c.getContents() instanceof TranslatableContents trans) {
            String key = trans.getKey();
            if (effectKeys.contains(key)) {
                return true;
            }
            if ("potion.withDuration".equals(key) || "potion.withAmplifier".equals(key) || "modest_meals.food.withChance".equals(key)) {
                for (Object arg : trans.getArgs()) {
                    if (arg instanceof Component subComp && isEffectTooltipLine(subComp, effectKeys)) {
                        return true;
                    }
                }
            }
        }
        for (Component sibling : c.getSiblings()) {
            if (isEffectTooltipLine(sibling, effectKeys)) {
                return true;
            }
        }
        return false;
    }
}
