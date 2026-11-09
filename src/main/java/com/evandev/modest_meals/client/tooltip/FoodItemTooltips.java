package com.evandev.modest_meals.client.tooltip;

import com.evandev.modest_meals.client.ModSprites;
import com.evandev.modest_meals.client.hud.StaminaSprites;
import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightCompat;
import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightSoupEffects;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.config.TooltipVisibility;
import com.evandev.modest_meals.food.BuiltInFoodEffects;
import com.evandev.modest_meals.food.EdibleBlockFoods;
import com.evandev.modest_meals.food.FoodEffect;
import com.evandev.modest_meals.food.FoodValues;
import com.evandev.modest_meals.food.ingredient.IngredientProfile;
import com.evandev.modest_meals.food.ingredient.IngredientProfileManager;
import com.evandev.modest_meals.food.ingredient.MealEffectManager;
import com.evandev.modest_meals.stamina.StaminaData;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.impl.HealthAdditionTrait;
import com.evandev.modest_meals.trait.impl.StaminaAdditionTrait;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

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

    public static void onTooltip(ItemStack stack, List<Component> lines, float tickRate) {
        if (stack.isEmpty()) {
            return;
        }


        List<FoodEffect> effects = foodEffects(stack);
        if (FoodValues.isFood(stack)) {
            removePreExistingFoodEffects(lines, effects);
        }

        Optional<EdibleBlockFoods.Servings> servings = EdibleBlockFoods.servingsOf(stack);
        ItemStack eaten = servings.map(EdibleBlockFoods.Servings::serving).orElse(stack);
        float scale = servings.map(EdibleBlockFoods.Servings::scale).orElse(1.0F);
        List<FoodEffect> eatenEffects = servings.isPresent() ? foodEffects(eaten) : effects;

        if (ModConfig.get().showFoodItemTooltips) {
            servings.ifPresent(serving -> lines.add(servingsLine(serving)));
            addStatRows(lines, eaten, scale);
            addDigestionRateLine(lines, eaten);
        }

        addEffectsAndTraits(lines, eaten, eatenEffects, tickRate, scale);
        addIngredientHint(lines, stack);
    }

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

    private static Component servingsLine(EdibleBlockFoods.Servings servings) {
        String key = servings.sliced() ? "modest_meals.tooltip.per_slice" : "modest_meals.tooltip.per_serving";
        return Component.translatable(key, servings.count()).withStyle(ChatFormatting.GRAY);
    }

    private static void addStatRows(List<Component> lines, ItemStack stack, float scale) {
        int halfHearts = Mth.ceil(FoodValues.healthPoints(stack) * scale);
        int halfStamina = 0;
        if (ModConfig.get().staminaSprint) {
            float levels = FoodValues.staminaLevels(stack) * scale;
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
        digestionRateLine(FoodValues.healthPoints(stack), FoodValues.healthDigestTicks(stack)).ifPresent(lines::add);
    }

    public static Optional<Component> digestionRateLine(float halfHearts, int digestTicks) {
        if (!ModConfig.get().showFoodRegenerationTooltips || !ModConfig.get().gradualHealthRegeneration) {
            return Optional.empty();
        }
        if (halfHearts <= 0.0F || digestTicks <= 0) {
            return Optional.empty();
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

        return Optional.of(Component.translatable(CONFIG_PREFIX + "template", Component.translatable(rate))
                .setStyle(Style.EMPTY.withColor(formatting)));
    }

    public static Optional<Component> previewLine(FoodTrait trait, float tickRate) {
        if (trait instanceof HealthAdditionTrait health) {
            return digestionRateLine(health.value() * ModConfig.get().traitGlobalValueMultiplier,
                    (int) (health.duration() * ModConfig.get().traitGlobalDurationMultiplier));
        }
        if (!hasTooltipLine(trait)) {
            return Optional.empty();
        }
        return Optional.ofNullable(trait.getTooltipComponent(1.0, 1.0, tickRate));
    }

    public static boolean hasTooltipLine(FoodTrait trait) {
        return !(trait instanceof HealthAdditionTrait || trait instanceof StaminaAdditionTrait);
    }

    private static List<FoodEffect> foodEffects(ItemStack stack) {
        List<FoodEffect> effects = FoodEffect.ofStack(stack);

        if (FarmersDelightCompat.isLoaded()) {
            List<FoodEffect> soupEffects = FarmersDelightSoupEffects.get(stack);
            if (!soupEffects.isEmpty()) {
                List<FoodEffect> combined = new ArrayList<>(effects);
                combined.addAll(soupEffects);
                return combined;
            }
        }
        return effects;
    }

    private static void addEffectsAndTraits(List<Component> lines, ItemStack stack,
                                            List<FoodEffect> effects, float tickRate,
                                            float scale) {
        if (!FoodValues.isFood(stack) || !isVisible(ModConfig.get().whenEatenTooltip)) {
            return;
        }

        List<Component> effectLines = new ArrayList<>();
        List<Component> traitLines = new ArrayList<>();

        List<FoodEffect> shownEffects = BuiltInFoodEffects.kept(stack, effects);
        List<FoodTrait> traits = new ArrayList<>();
        double valMult = ModConfig.get().traitGlobalValueMultiplier * scale;
        double durMult = ModConfig.get().traitGlobalDurationMultiplier * scale;

        if (ModConfig.get().showFoodTraitTooltips) {
            for (FoodTrait trait : FoodValues.effectiveTraits(stack)) {
                if (hasTooltipLine(trait)) {
                    traits.add(trait);
                }
            }
        }

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

    private static void removePreExistingFoodEffects(List<Component> lines,
                                                     List<FoodEffect> effects) {
        Set<String> effectKeys = new HashSet<>();
        for (FoodEffect pe : effects) {
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
            List<FoodEffect> effects,
            float tickRate,
            List<Component> effectLines
    ) {
        for (FoodEffect entry : effects) {
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
