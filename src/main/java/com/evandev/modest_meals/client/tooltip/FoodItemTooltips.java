package com.evandev.modest_meals.client.tooltip;

import com.evandev.modest_meals.client.ModSprites;
import com.evandev.modest_meals.client.hud.StaminaSprites;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.food.FoodValues;
import com.evandev.modest_meals.stamina.StaminaData;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.impl.HealthAdditionTrait;
import com.evandev.modest_meals.trait.impl.StaminaAdditionTrait;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

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

        if (ModConfig.get().showFoodItemTooltips) {
            addStatRows(lines, stack);
            addDigestionRateLine(lines, stack);
        }

        if (ModConfig.get().showFoodTraitTooltips) {
            addTraitLines(lines, stack);
        }
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

    private static void addTraitLines(List<Component> lines, ItemStack stack) {
        if (!FoodValues.isFood(stack)) {
            return;
        }

        List<FoodTrait> traits = FoodValues.effectiveTraits(stack);
        if (traits.isEmpty()) {
            return;
        }

        double valMult = ModConfig.get().traitGlobalValueMultiplier;
        double durMult = ModConfig.get().traitGlobalDurationMultiplier;
        boolean headerAdded = false;

        for (FoodTrait trait : traits) {
            if (trait instanceof HealthAdditionTrait || trait instanceof StaminaAdditionTrait) {
                continue;
            }
            if (!headerAdded) {
                lines.add(Component.translatable("modest_meals.trait.header.consumed").withStyle(ChatFormatting.GRAY));
                headerAdded = true;
            }
            lines.add(trait.getTooltipComponent(valMult, durMult));
        }
    }
}
