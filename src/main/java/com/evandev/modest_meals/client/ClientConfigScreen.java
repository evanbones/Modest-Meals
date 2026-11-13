package com.evandev.modest_meals.client;

import com.evandev.modest_meals.client.gui.FoodEditorScreen;
import com.evandev.modest_meals.config.*;
import com.evandev.modest_meals.stamina.Corner;
import com.evandev.modest_meals.stamina.StaminaRegain;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientConfigScreen {

    public static Screen create(Screen parent) {
        ModConfig defaults = new ModConfig();
        defaults.validateDefaults();
        ModConfig config = ModConfig.local();
        config.validateDefaults();
        ModConfig gameplay = ModConfig.isServerControlled() ? ModConfig.get() : config;

        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.modest_meals.title"))
                .save(ModConfig::save);

        ConfigCategory.Builder gameplayCategoryBuilder = ConfigCategory.createBuilder()
                .name(Component.translatable("config.modest_meals.category.gameplay"))
                .tooltip(Component.translatable("config.modest_meals.category.gameplay.tooltip"));

        gameplayCategoryBuilder.group(OptionGroup.createBuilder()
                .name(Component.translatable("config.modest_meals.group.hunger"))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.group.hunger.tooltip")))
                .option(createBoolOption("disable_hunger", defaults.disableHunger, () -> gameplay.disableHunger, val -> gameplay.disableHunger = val))
                .option(createEnumOption("hunger_effect", HungerEffectOption.class, defaults.hungerEffect, () -> gameplay.hungerEffect, val -> gameplay.hungerEffect = val, HungerEffectOption::getTitle))
                .option(createStringOption("hunger_replacement_effect", defaults.hungerReplacementEffect, () -> gameplay.hungerReplacementEffect, val -> gameplay.hungerReplacementEffect = val))
                .option(createFloatOption("hunger_replacement_duration_multiplier", defaults.hungerReplacementDurationMultiplier, 0.1F, 10.0F, 0.1F, () -> gameplay.hungerReplacementDurationMultiplier, val -> gameplay.hungerReplacementDurationMultiplier = val))
                .option(createBoolOption("instant_eating", defaults.instantEating, () -> gameplay.instantEating, val -> gameplay.instantEating = val))
                .option(createBoolOption("always_eat", defaults.alwaysEat, () -> gameplay.alwaysEat, val -> gameplay.alwaysEat = val))
                .build());

        gameplayCategoryBuilder.group(OptionGroup.createBuilder()
                .name(Component.translatable("config.modest_meals.group.health_regeneration"))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.group.health_regeneration.tooltip")))
                .option(createBoolOption("gradual_health_regeneration", defaults.gradualHealthRegeneration, () -> gameplay.gradualHealthRegeneration, val -> gameplay.gradualHealthRegeneration = val))
                .option(createFloatOption("gradual_health_regeneration_speed", defaults.gradualHealthRegenerationSpeed, 0.1F, 10.0F, 0.1F, () -> gameplay.gradualHealthRegenerationSpeed, val -> gameplay.gradualHealthRegenerationSpeed = val))
                .option(createEnumOption("regeneration_at_full_health", RegenerationAtFullHealthOption.class, defaults.regenerationAtFullHealth, () -> gameplay.regenerationAtFullHealth, val -> gameplay.regenerationAtFullHealth = val, RegenerationAtFullHealthOption::getTitle))
                .option(createBoolOption("stamina_natural_regeneration", defaults.staminaNaturalRegeneration, () -> gameplay.staminaNaturalRegeneration, val -> gameplay.staminaNaturalRegeneration = val))
                .option(createIntOption("stamina_natural_regeneration_fast_interval", defaults.staminaNaturalRegenerationFastInterval, 1, 40, 1, () -> gameplay.staminaNaturalRegenerationFastInterval, val -> gameplay.staminaNaturalRegenerationFastInterval = val))
                .option(createIntOption("stamina_natural_regeneration_interval", defaults.staminaNaturalRegenerationInterval, 10, 200, 5, () -> gameplay.staminaNaturalRegenerationInterval, val -> gameplay.staminaNaturalRegenerationInterval = val))
                .option(createIntOption("stamina_natural_regeneration_threshold", defaults.staminaNaturalRegenerationThreshold, 1, 20, 1, () -> gameplay.staminaNaturalRegenerationThreshold, val -> gameplay.staminaNaturalRegenerationThreshold = val))
                .option(createFloatOption("stamina_natural_regeneration_drain", defaults.staminaNaturalRegenerationDrain, 0.0F, 5.0F, 0.1F, () -> gameplay.staminaNaturalRegenerationDrain, val -> gameplay.staminaNaturalRegenerationDrain = val))
                .build());

        gameplayCategoryBuilder.group(OptionGroup.createBuilder()
                .name(Component.translatable("config.modest_meals.group.mechanics"))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.group.mechanics.tooltip")))
                .option(createBoolOption("stamina_sprint", defaults.staminaSprint, () -> gameplay.staminaSprint, val -> gameplay.staminaSprint = val))
                .option(createBoolOption("stamina_infinite_peaceful", defaults.staminaInfinitePeaceful, () -> gameplay.staminaInfinitePeaceful, val -> gameplay.staminaInfinitePeaceful = val))
                .option(createEnumOption("stamina_regain_when_moving", StaminaRegain.class, defaults.staminaRegainWhenMoving, () -> gameplay.staminaRegainWhenMoving, val -> gameplay.staminaRegainWhenMoving = val, StaminaRegain::getTitle))
                .option(createIntOption("stamina_duration", defaults.staminaDuration, 1, 600, 1, () -> gameplay.staminaDuration, val -> gameplay.staminaDuration = val))
                .option(createIntOption("stamina_regain_time", defaults.staminaRegainTime, 1, 600, 1, () -> gameplay.staminaRegainTime, val -> gameplay.staminaRegainTime = val))
                .option(createIntOption("stamina_recharge", defaults.staminaRecharge, 1, 600, 1, () -> gameplay.staminaRecharge, val -> gameplay.staminaRecharge = val))
                .option(createIntOption("stamina_cooldown", defaults.staminaCooldown, 0, 60, 1, () -> gameplay.staminaCooldown, val -> gameplay.staminaCooldown = val))
                .option(createBoolOption("stamina_exhaustion_lockout", defaults.staminaExhaustionLockout, () -> gameplay.staminaExhaustionLockout, val -> gameplay.staminaExhaustionLockout = val))
                .option(createBoolOption("stamina_sprinting", defaults.staminaSprinting, () -> gameplay.staminaSprinting, val -> gameplay.staminaSprinting = val))
                .option(createBoolOption("stamina_gliding", defaults.staminaGliding, () -> gameplay.staminaGliding, val -> gameplay.staminaGliding = val))
                .option(createBoolOption("stamina_elytra", defaults.staminaElytra, () -> gameplay.staminaElytra, val -> gameplay.staminaElytra = val))
                .option(createBoolOption("stamina_shield_blocking", defaults.staminaShieldBlocking, () -> gameplay.staminaShieldBlocking, val -> gameplay.staminaShieldBlocking = val))
                .option(createFloatOption("stamina_shield_blocking_cost", defaults.staminaShieldBlockingCost, 0.0F, 5.0F, 0.1F, () -> gameplay.staminaShieldBlockingCost, val -> gameplay.staminaShieldBlockingCost = val))
                .option(createFloatOption("stamina_parry_restore", defaults.staminaParryRestore, 0.0F, 20.0F, 0.5F, () -> gameplay.staminaParryRestore, val -> gameplay.staminaParryRestore = val))
                .option(createFloatOption("stamina_combat_roll_cost", defaults.staminaCombatRollCost, 0.0F, 20.0F, 0.5F, () -> gameplay.staminaCombatRollCost, val -> gameplay.staminaCombatRollCost = val))
                .option(createFloatOption("stamina_hand_crank_cost", defaults.staminaHandCrankCost, 0.0F, 2.0F, 0.01F, () -> gameplay.staminaHandCrankCost, val -> gameplay.staminaHandCrankCost = val))
                .option(createEnumOption("sprinting", SprintingOption.class, defaults.sprinting, () -> gameplay.sprinting, val -> gameplay.sprinting = val, SprintingOption::getTitle))
                .option(createIntOption("sprinting_health_limit", defaults.sprintingHealthLimit, 1, 20, 1, () -> gameplay.sprintingHealthLimit, val -> gameplay.sprintingHealthLimit = val))
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
                .option(createIntOption("stamina_bar_offset_x", defaults.staminaBarOffsetX, -100, 100, 1, () -> config.staminaBarOffsetX, val -> config.staminaBarOffsetX = val))
                .option(createIntOption("stamina_bar_offset_y", defaults.staminaBarOffsetY, -100, 100, 1, () -> config.staminaBarOffsetY, val -> config.staminaBarOffsetY = val))
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

        ConfigCategory.Builder foodEffectsCategoryBuilder = ConfigCategory.createBuilder()
                .name(Component.translatable("config.modest_meals.category.food_effects"))
                .tooltip(Component.translatable("config.modest_meals.category.food_effects.tooltip"));

        foodEffectsCategoryBuilder.group(OptionGroup.createBuilder()
                .name(Component.translatable("config.modest_meals.group.food_editor"))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.group.food_editor.tooltip")))
                .option(ButtonOption.createBuilder()
                        .name(Component.translatable("config.modest_meals.option.open_food_editor"))
                        .description(OptionDescription.of(Component.translatable("config.modest_meals.option.open_food_editor.tooltip")))
                        .action((yaclScreen, opt) -> Minecraft.getInstance().setScreen(new FoodEditorScreen(yaclScreen)))
                        .build())
                .option(createBoolOption("show_food_trait_tooltips", defaults.showFoodTraitTooltips, () -> config.showFoodTraitTooltips, val -> config.showFoodTraitTooltips = val))
                .option(createEnumOption("when_eaten_tooltip", TooltipVisibility.class, defaults.whenEatenTooltip, () -> config.whenEatenTooltip, val -> config.whenEatenTooltip = val, TooltipVisibility::getTitle))
                .option(createEnumOption("ingredient_tooltip", TooltipVisibility.class, defaults.ingredientTooltip, () -> config.ingredientTooltip, val -> config.ingredientTooltip = val, TooltipVisibility::getTitle))
                .build());

        foodEffectsCategoryBuilder.group(OptionGroup.createBuilder()
                .name(Component.translatable("config.modest_meals.group.food_multipliers"))
                .description(OptionDescription.of(Component.translatable("config.modest_meals.group.food_multipliers.tooltip")))
                .option(createBoolOption("food_effects", defaults.foodEffects, () -> gameplay.foodEffects, val -> gameplay.foodEffects = val))
                .option(createFloatOption("trait_global_value_multiplier", defaults.traitGlobalValueMultiplier, 0.1F, 10.0F, 0.1F, () -> gameplay.traitGlobalValueMultiplier, val -> gameplay.traitGlobalValueMultiplier = val))
                .option(createFloatOption("trait_global_duration_multiplier", defaults.traitGlobalDurationMultiplier, 0.1F, 10.0F, 0.1F, () -> gameplay.traitGlobalDurationMultiplier, val -> gameplay.traitGlobalDurationMultiplier = val))
                .build());

        builder.category(foodEffectsCategoryBuilder.build());

        return builder.build().generateScreen(parent);
    }

    private static OptionDescription description(String name) {
        Component tooltip = Component.translatable("config.modest_meals.option." + name + ".tooltip");
        if (!ModConfig.isServerControlled(name)) {
            return OptionDescription.of(tooltip);
        }
        return OptionDescription.of(tooltip, Component.translatable("config.modest_meals.server_controlled")
                .withStyle(ChatFormatting.GOLD));
    }

    private static Option<Boolean> createBoolOption(String name, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(description(name))
                .available(!ModConfig.isServerControlled(name))
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    private static Option<Integer> createIntOption(String name, int defaultValue, int min, int max, int step, Supplier<Integer> getter, Consumer<Integer> setter) {
        return Option.<Integer>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(description(name))
                .available(!ModConfig.isServerControlled(name))
                .binding(defaultValue, getter, setter)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(step))
                .build();
    }

    private static Option<Float> createFloatOption(String name, float defaultValue, float min, float max, float step, Supplier<Float> getter, Consumer<Float> setter) {
        return Option.<Float>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(description(name))
                .available(!ModConfig.isServerControlled(name))
                .binding(defaultValue, getter, setter)
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(step).formatValue(
                        value -> Component.literal(String.format("%,.2f", value))
                ))
                .build();
    }

    private static Option<Color> createColorOption(String name, Color defaultValue, Supplier<Color> getter, Consumer<Color> setter) {
        return Option.<Color>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(description(name))
                .available(!ModConfig.isServerControlled(name))
                .binding(defaultValue, getter, setter)
                .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(false))
                .build();
    }

    private static <T extends Enum<T>> Option<T> createEnumOption(String name, Class<T> enumClass, T defaultValue, Supplier<T> getter, Consumer<T> setter, ValueFormatter<T> formatter) {
        return Option.<T>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(description(name))
                .available(!ModConfig.isServerControlled(name))
                .binding(defaultValue, getter, setter)
                .controller(opt -> EnumControllerBuilder.create(opt).enumClass(enumClass).formatValue(formatter))
                .build();
    }

    private static Option<String> createStringOption(String name, String defaultValue, Supplier<String> getter, Consumer<String> setter) {
        return Option.<String>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(description(name))
                .available(!ModConfig.isServerControlled(name))
                .binding(defaultValue, getter, setter)
                .controller(StringControllerBuilder::create)
                .build();
    }

}
