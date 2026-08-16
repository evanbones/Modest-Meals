package com.evandev.modest_meals.client.tooltip;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.food.EdibleBlockFoods;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class FoodItemTooltips {
    private static final String CONFIG_PREFIX = "gui.modest_meals.regeneration_tooltip.";

    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        List<Component> lines = event.getToolTip();

        if (ModConfig.get().disableHunger && ModConfig.get().showFoodItemTooltips) {
            FoodProperties foodProperties = stack.get(DataComponents.FOOD);
            if (foodProperties == null) {
                var foodPropertiesOptional = EdibleBlockFoods.getFoodProperties(stack.getItem());
                if (foodPropertiesOptional.isPresent()) {
                    foodProperties = foodPropertiesOptional.get();
                }
            }

            if (foodProperties != null) {
                int foodNutrition = ModConfig.getFoodHealth(stack, foodProperties);
                if (foodNutrition > 0) {
                    lines.add(new FoodHealthTextComponent(foodNutrition));

                    if (ModConfig.get().showFoodRegenerationTooltips && ModConfig.get().gradualHealthRegeneration && ModConfig.get().saturationBasedRegeneration) {
                        float saturation = Math.max(0.01F, foodProperties.saturation());
                        float regenerationRatio = (float) foodNutrition / saturation;
                        String regenerationRate;
                        ChatFormatting formatting;

                        if (regenerationRatio <= 0.5F) {
                            regenerationRate = CONFIG_PREFIX + "super_fast";
                            formatting = ChatFormatting.DARK_PURPLE;
                        } else if (regenerationRatio <= 0.8F) {
                            regenerationRate = CONFIG_PREFIX + "very_fast";
                            formatting = ChatFormatting.DARK_GREEN;
                        } else if (regenerationRatio <= 1.6F) {
                            regenerationRate = CONFIG_PREFIX + "fast";
                            formatting = ChatFormatting.GREEN;
                        } else if (regenerationRatio <= 2.5F) {
                            regenerationRate = CONFIG_PREFIX + "slow";
                            formatting = ChatFormatting.RED;
                        } else {
                            regenerationRate = CONFIG_PREFIX + "very_slow";
                            formatting = ChatFormatting.DARK_RED;
                        }

                        lines.add(
                                Component.translatable(CONFIG_PREFIX + "template", Component.translatable(regenerationRate))
                                        .setStyle(Style.EMPTY.withColor(formatting.getColor()))
                        );
                    }
                }
            }
        }

        if (ModConfig.get().showFoodTraitTooltips) {
            List<FoodTrait> traits = FoodTraitManager.getTraits(stack);
            if (!traits.isEmpty()) {
                if (stack.has(DataComponents.FOOD) || EdibleBlockFoods.getFoodProperties(stack.getItem()).isPresent()) {
                    lines.add(Component.translatable("modest_meals.trait.header.consumed").withStyle(ChatFormatting.GRAY));
                } else {
                    lines.add(Component.translatable("modest_meals.trait.header.ingredient").withStyle(ChatFormatting.GRAY));
                }
                double valMult = ModConfig.get().traitGlobalValueMultiplier;
                double durMult = ModConfig.get().traitGlobalDurationMultiplier;
                for (FoodTrait trait : traits) {
                    lines.add(trait.getTooltipComponent(valMult, durMult));
                }
            }
        }
    }

    public record FoodHealthTextComponent(int foodNutrition) implements Component, FormattedCharSequence {
        private static final List<Component> EMPTY_SIBLINGS = new ArrayList<>();

        @Override
        public Style getStyle() {
            return Style.EMPTY;
        }

        @Override
        public ComponentContents getContents() {
            return PlainTextContents.EMPTY;
        }

        @Override
        public List<Component> getSiblings() {
            return EMPTY_SIBLINGS;
        }

        @Override
        public FormattedCharSequence getVisualOrderText() {
            return this;
        }

        @Override
        public boolean accept(FormattedCharSink visitor) {
            return StringDecomposer.iterateFormatted(this, getStyle(), visitor);
        }

        public FoodHealthTooltipComponent getComponent() {
            return FoodHealthTooltipComponent.init(foodNutrition);
        }
    }

    public static class FoodHealthTooltipComponent extends ClientTextTooltip {
        private final int heartsCount;
        private final boolean lastHeartIsHalf;

        FoodHealthTooltipComponent(String text, int foodNutrition) {
            super(Component.literal(text).getVisualOrderText());
            heartsCount = (int) Math.ceil(foodNutrition / 2.0F);
            lastHeartIsHalf = foodNutrition % 2 != 0;
        }

        public static FoodHealthTooltipComponent init(int foodNutrition) {
            String text = "";
            if (foodNutrition > 20) {
                text = "x%d".formatted(foodNutrition / 2);
                if (foodNutrition % 2 > 0) {
                    text += ".5";
                }
                foodNutrition = 2;
            }
            return new FoodHealthTooltipComponent(text, foodNutrition);
        }

        @Override
        public int getHeight() {
            return 14;
        }

        @Override
        public int getWidth(Font font) {
            return heartsCount * 9 + super.getWidth(font);
        }

        @Override
        public void renderText(Font font, int mouseX, int mouseY, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
            super.renderText(font, mouseX + 12, mouseY + 2, matrix, bufferSource);
        }

        @Override
        public void renderImage(Font font, int x, int y, GuiGraphics context) {
            y += 2;
            for (int i = 0; i < heartsCount - 1; i++) {
                int textureX = x + i * 9;
                context.blitSprite(Gui.HeartType.CONTAINER.getSprite(false, false, false), textureX, y, 9, 9);
                context.blitSprite(Gui.HeartType.NORMAL.getSprite(false, false, false), textureX, y, 9, 9);
            }
            int textureX = x + (heartsCount - 1) * 9;
            context.blitSprite(Gui.HeartType.CONTAINER.getSprite(false, lastHeartIsHalf, false), textureX, y, 9, 9);
            context.blitSprite(Gui.HeartType.NORMAL.getSprite(false, lastHeartIsHalf, false), textureX, y, 9, 9);
        }
    }
}
