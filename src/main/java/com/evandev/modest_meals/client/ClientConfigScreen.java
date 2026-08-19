package com.evandev.modest_meals.client;

import com.evandev.modest_meals.config.*;
import com.evandev.modest_meals.stamina.Corner;
import com.evandev.modest_meals.stamina.StaminaRegain;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientConfigScreen {

    public static Screen create(Screen parent) {
        ModConfig defaults = new ModConfig();
        defaults.validateDefaults();
        ModConfig config = ModConfig.get();
        config.validateDefaults();

        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.modest_meals.title"))
                .save(ModConfig::save);

        ConfigCategory.Builder gameplayCategoryBuilder = ConfigCategory.createBuilder()
                .name(Component.translatable("config.modest_meals.category.gameplay"))
                .tooltip(Component.translatable("config.modest_meals.category.gameplay.tooltip"));

        gameplayCategoryBuilder.group(OptionGroup.createBuilder()
                .name(Component.translatable("config.modest_meals.group.hunger"))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.group.hunger.tooltip")))
                .option(createBoolOption("disable_hunger", defaults.disableHunger, () -> config.disableHunger, val -> config.disableHunger = val))
                .option(createEnumOption("hunger_effect", HungerEffectOption.class, defaults.hungerEffect, () -> config.hungerEffect, val -> config.hungerEffect = val, HungerEffectOption::getTitle))
                .option(createStringOption("hunger_replacement_effect", defaults.hungerReplacementEffect, () -> config.hungerReplacementEffect, val -> config.hungerReplacementEffect = val))
                .option(createFloatOption("hunger_replacement_duration_multiplier", defaults.hungerReplacementDurationMultiplier, 0.1F, 10.0F, 0.1F, () -> config.hungerReplacementDurationMultiplier, val -> config.hungerReplacementDurationMultiplier = val))
                .option(createBoolOption("instant_eating", defaults.instantEating, () -> config.instantEating, val -> config.instantEating = val))
                .build());

        gameplayCategoryBuilder.group(OptionGroup.createBuilder()
                .name(Component.translatable("config.modest_meals.group.health_regeneration"))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.group.health_regeneration.tooltip")))
                .option(createBoolOption("gradual_health_regeneration", defaults.gradualHealthRegeneration, () -> config.gradualHealthRegeneration, val -> config.gradualHealthRegeneration = val))
                .option(createFloatOption("gradual_health_regeneration_speed", defaults.gradualHealthRegenerationSpeed, 0.1F, 10.0F, 0.1F, () -> config.gradualHealthRegenerationSpeed, val -> config.gradualHealthRegenerationSpeed = val))
                .option(createEnumOption("regeneration_at_full_health", RegenerationAtFullHealthOption.class, defaults.regenerationAtFullHealth, () -> config.regenerationAtFullHealth, val -> config.regenerationAtFullHealth = val, RegenerationAtFullHealthOption::getTitle))
                .build());

        gameplayCategoryBuilder.group(OptionGroup.createBuilder()
                .name(Component.translatable("config.modest_meals.group.mechanics"))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.group.mechanics.tooltip")))
                .option(createBoolOption("stamina_sprint", defaults.staminaSprint, () -> config.staminaSprint, val -> config.staminaSprint = val))
                .option(createBoolOption("stamina_infinite_peaceful", defaults.staminaInfinitePeaceful, () -> config.staminaInfinitePeaceful, val -> config.staminaInfinitePeaceful = val))
                .option(createEnumOption("stamina_regain_when_moving", StaminaRegain.class, defaults.staminaRegainWhenMoving, () -> config.staminaRegainWhenMoving, val -> config.staminaRegainWhenMoving = val, StaminaRegain::getTitle))
                .option(createIntOption("stamina_duration", defaults.staminaDuration, 1, 60, 1, () -> config.staminaDuration, val -> config.staminaDuration = val))
                .option(createIntOption("stamina_recharge", defaults.staminaRecharge, 1, 60, 1, () -> config.staminaRecharge, val -> config.staminaRecharge = val))
                .option(createIntOption("stamina_cooldown", defaults.staminaCooldown, 0, 60, 1, () -> config.staminaCooldown, val -> config.staminaCooldown = val))
                .option(createEnumOption("sprinting", SprintingOption.class, defaults.sprinting, () -> config.sprinting, val -> config.sprinting = val, SprintingOption::getTitle))
                .option(createIntOption("sprinting_health_limit", defaults.sprintingHealthLimit, 1, 20, 1, () -> config.sprintingHealthLimit, val -> config.sprintingHealthLimit = val))
                .build());

        builder.category(gameplayCategoryBuilder.build());

        ConfigCategory.Builder hudCategoryBuilder = ConfigCategory.createBuilder()
                .name(Component.translatable("config.modest_meals.category.hud"))
                .tooltip(Component.translatable("config.modest_meals.category.hud.tooltip"));

        hudCategoryBuilder.group(OptionGroup.createBuilder()
                .name(Component.translatable("config.modest_meals.group.stamina_bar"))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.group.stamina_bar.tooltip")))
                .option(createEnumOption("hud_layout", HudLayoutOption.class, defaults.hudLayout, () -> config.hudLayout, val -> config.hudLayout = val, HudLayoutOption::getTitle))
                .option(createBoolOption("hide_hunger_bar", defaults.hideHungerBar, () -> config.hideHungerBar, val -> config.hideHungerBar = val))
                .option(createBoolOption("hide_stamina_bar", defaults.hideStaminaBar, () -> config.hideStaminaBar, val -> config.hideStaminaBar = val))
                .option(createBoolOption("hide_stamina_bar_moving", defaults.hideStaminaBarMoving, () -> config.hideStaminaBarMoving, val -> config.hideStaminaBarMoving = val))
                .option(createBoolOption("hide_stamina_bar_cooldown", defaults.hideStaminaBarCooldown, () -> config.hideStaminaBarCooldown, val -> config.hideStaminaBarCooldown = val))
                .option(createBoolOption("hide_stamina_bar_inactive", defaults.hideStaminaBarInactive, () -> config.hideStaminaBarInactive, val -> config.hideStaminaBarInactive = val))
                .option(createBoolOption("highlight_stamina_bar", defaults.highlightStaminaBar, () -> config.highlightStaminaBar, val -> config.highlightStaminaBar = val))
                .option(createBoolOption("flash_stamina_bar_when_full", defaults.flashStaminaBarWhenFull, () -> config.flashStaminaBarWhenFull, val -> config.flashStaminaBarWhenFull = val))
                .option(createIntOption("flash_stamina_bar_at", defaults.flashStaminaBarAt, 0, 20, 1, () -> config.flashStaminaBarAt, val -> config.flashStaminaBarAt = val))
                .option(createBoolOption("highlight_restored_stamina", defaults.highlightRestoredStamina, () -> config.highlightRestoredStamina, val -> config.highlightRestoredStamina = val))
                .option(createColorOption("restored_stamina_overlay_color", defaults.restoredStaminaOverlayColor, () -> config.restoredStaminaOverlayColor, val -> config.restoredStaminaOverlayColor = val))
                .build());

        hudCategoryBuilder.group(OptionGroup.createBuilder()
                .name(Component.translatable("config.modest_meals.group.hud_restored_health"))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.group.hud_restored_health.tooltip")))
                .option(createBoolOption("highlight_restored_hearts", defaults.highlightRestoredHearts, () -> config.highlightRestoredHearts, val -> config.highlightRestoredHearts = val))
                .option(createEnumOption("restored_hearts_texture", HeartTextureOption.class, defaults.restoredHeartsTexture, () -> config.restoredHeartsTexture, val -> config.restoredHeartsTexture = val, HeartTextureOption::getTitle))
                .option(createColorOption("restored_hearts_overlay_color", defaults.restoredHeartsOverlayColor, () -> config.restoredHeartsOverlayColor, val -> config.restoredHeartsOverlayColor = val))
                .build());

        hudCategoryBuilder.group(OptionGroup.createBuilder()
                .name(Component.translatable("config.modest_meals.group.hud_regenerated_health"))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.group.hud_regenerated_health.tooltip")))
                .option(createBoolOption("highlight_regenerated_hearts", defaults.highlightRegeneratedHearts, () -> config.highlightRegeneratedHearts, val -> config.highlightRegeneratedHearts = val))
                .option(createEnumOption("regenerated_hearts_texture", HeartTextureOption.class, defaults.regeneratedHeartsTexture, () -> config.regeneratedHeartsTexture, val -> config.regeneratedHeartsTexture = val, HeartTextureOption::getTitle))
                .option(createColorOption("regenerated_hearts_overlay_color", defaults.regeneratedHeartsOverlayColor, () -> config.regeneratedHeartsOverlayColor, val -> config.regeneratedHeartsOverlayColor = val))
                .option(createFloatOption("regenerated_hearts_opacity_min", defaults.regeneratedHeartsOpacityMin, 0.0F, 1.0F, 0.01F, () -> config.regeneratedHeartsOpacityMin, val -> config.regeneratedHeartsOpacityMin = val))
                .option(createFloatOption("regenerated_hearts_opacity_max", defaults.regeneratedHeartsOpacityMax, 0.0F, 1.0F, 0.01F, () -> config.regeneratedHeartsOpacityMax, val -> config.regeneratedHeartsOpacityMax = val))
                .option(createIntOption("regenerated_hearts_blinking_period", defaults.regeneratedHeartsBlinkingPeriod, 500, 5000, 100, () -> config.regeneratedHeartsBlinkingPeriod, val -> config.regeneratedHeartsBlinkingPeriod = val))
                .build());

        hudCategoryBuilder.group(OptionGroup.createBuilder()
                .name(Component.translatable("config.modest_meals.group.hud_tooltips"))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.group.hud_tooltips.tooltip")))
                .option(createBoolOption("show_food_item_tooltips", defaults.showFoodItemTooltips, () -> config.showFoodItemTooltips, val -> config.showFoodItemTooltips = val))
                .option(createBoolOption("show_food_regeneration_tooltips", defaults.showFoodRegenerationTooltips, () -> config.showFoodRegenerationTooltips, val -> config.showFoodRegenerationTooltips = val))
                .build());

        hudCategoryBuilder.group(OptionGroup.createBuilder()
                .name(Component.translatable("config.modest_meals.group.stamina_text"))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.group.stamina_text.tooltip")))
                .option(createBoolOption("show_stamina_text", defaults.showStaminaText, () -> config.showStaminaText, val -> config.showStaminaText = val))
                .option(createBoolOption("use_dynamic_stamina_color", defaults.useDynamicStaminaColor, () -> config.useDynamicStaminaColor, val -> config.useDynamicStaminaColor = val))
                .option(createBoolOption("alt_stamina_show_on_active", defaults.altStaminaShowOnActive, () -> config.altStaminaShowOnActive, val -> config.altStaminaShowOnActive = val))
                .option(createEnumOption("alt_stamina_corner", Corner.class, defaults.altStaminaCorner, () -> config.altStaminaCorner, val -> config.altStaminaCorner = val, Corner::getTitle))
                .option(createIntOption("alt_stamina_offset_x", defaults.altStaminaOffsetX, -100, 100, 1, () -> config.altStaminaOffsetX, val -> config.altStaminaOffsetX = val))
                .option(createIntOption("alt_stamina_offset_y", defaults.altStaminaOffsetY, -100, 100, 1, () -> config.altStaminaOffsetY, val -> config.altStaminaOffsetY = val))
                .option(createBoolOption("alt_stamina_shadow", defaults.altStaminaShadow, () -> config.altStaminaShadow, val -> config.altStaminaShadow = val))
                .option(createStringOption("alt_stamina_text", defaults.altStaminaText, () -> config.altStaminaText, val -> config.altStaminaText = val))
                .build());

        builder.category(hudCategoryBuilder.build());

        return builder.build().generateScreen(parent);
    }

    private static Option<Boolean> createBoolOption(String name, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.option." + name + ".tooltip")))
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    private static Option<Integer> createIntOption(String name, int defaultValue, int min, int max, int step, Supplier<Integer> getter, Consumer<Integer> setter) {
        return Option.<Integer>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.option." + name + ".tooltip")))
                .binding(defaultValue, getter, setter)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(step))
                .build();
    }

    private static Option<Float> createFloatOption(String name, float defaultValue, float min, float max, float step, Supplier<Float> getter, Consumer<Float> setter) {
        return Option.<Float>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.option." + name + ".tooltip")))
                .binding(defaultValue, getter, setter)
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(step).formatValue(
                        value -> Component.literal(String.format("%,.2f", value))
                ))
                .build();
    }

    private static Option<Color> createColorOption(String name, Color defaultValue, Supplier<Color> getter, Consumer<Color> setter) {
        return Option.<Color>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.option." + name + ".tooltip")))
                .binding(defaultValue, getter, setter)
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                .build();
    }

    private static <T extends Enum<T>> Option<T> createEnumOption(String name, Class<T> enumClass, T defaultValue, Supplier<T> getter, Consumer<T> setter, dev.isxander.yacl3.api.controller.ValueFormatter<T> formatter) {
        return Option.<T>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.option." + name + ".tooltip")))
                .binding(defaultValue, getter, setter)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(enumClass).formatValue(formatter))
                .build();
    }

    private static Option<String> createStringOption(String name, String defaultValue, Supplier<String> getter, Consumer<String> setter) {
        return Option.<String>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.option." + name + ".tooltip")))
                .binding(defaultValue, getter, setter)
                .controller(StringControllerBuilder::create)
                .build();
    }

}
