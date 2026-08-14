package com.evandev.modest_meals.client;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.stamina.Corner;
import com.evandev.modest_meals.stamina.StaminaRegain;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientConfigScreen {

    public static Screen create(Screen parent) {
        ModConfig defaults = new ModConfig();
        ModConfig config = ModConfig.get();

        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.modest_meals.title"))
                .save(ModConfig::save);

        ConfigCategory mechanicsCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("config.modest_meals.category.gameplay"))
                .tooltip(Component.translatable("config.modest_meals.category.gameplay.tooltip"))
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("config.modest_meals.group.mechanics"))
                        .option(createBoolOption("stamina_sprint", defaults.staminaSprint, () -> config.staminaSprint, val -> config.staminaSprint = val))
                        .option(createBoolOption("disable_hunger", defaults.disableHunger, () -> config.disableHunger, val -> config.disableHunger = val))
                        .option(createBoolOption("stamina_infinite_peaceful", defaults.staminaInfinitePeaceful, () -> config.staminaInfinitePeaceful, val -> config.staminaInfinitePeaceful = val))
                        .option(createEnumOption("stamina_regain_when_moving", StaminaRegain.class, defaults.staminaRegainWhenMoving, () -> config.staminaRegainWhenMoving, val -> config.staminaRegainWhenMoving = val, StaminaRegain::getTitle))
                        .option(createIntOption("stamina_duration", defaults.staminaDuration, 1, 60, 1, () -> config.staminaDuration, val -> config.staminaDuration = val))
                        .option(createIntOption("stamina_recharge", defaults.staminaRecharge, 1, 60, 1, () -> config.staminaRecharge, val -> config.staminaRecharge = val))
                        .option(createIntOption("stamina_cooldown", defaults.staminaCooldown, 0, 60, 1, () -> config.staminaCooldown, val -> config.staminaCooldown = val))
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("config.modest_meals.group.effects"))
                        .option(createBoolOption("stamina_saturation_effect", defaults.staminaSaturationEffect, () -> config.staminaSaturationEffect, val -> config.staminaSaturationEffect = val))
                        .option(createBoolOption("stamina_hunger_effect", defaults.staminaHungerEffect, () -> config.staminaHungerEffect, val -> config.staminaHungerEffect = val))
                        .build())
                .build();

        ConfigCategory hudCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("config.modest_meals.category.hud"))
                .tooltip(Component.translatable("config.modest_meals.category.hud.tooltip"))
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("config.modest_meals.group.stamina_bar"))
                        .option(createBoolOption("hide_hunger_bar", defaults.hideHungerBar, () -> config.hideHungerBar, val -> config.hideHungerBar = val))
                        .option(createBoolOption("hide_stamina_bar", defaults.hideStaminaBar, () -> config.hideStaminaBar, val -> config.hideStaminaBar = val))
                        .option(createBoolOption("hide_stamina_bar_moving", defaults.hideStaminaBarMoving, () -> config.hideStaminaBarMoving, val -> config.hideStaminaBarMoving = val))
                        .option(createBoolOption("hide_stamina_bar_cooldown", defaults.hideStaminaBarCooldown, () -> config.hideStaminaBarCooldown, val -> config.hideStaminaBarCooldown = val))
                        .option(createBoolOption("hide_stamina_bar_inactive", defaults.hideStaminaBarInactive, () -> config.hideStaminaBarInactive, val -> config.hideStaminaBarInactive = val))
                        .option(createBoolOption("highlight_stamina_bar", defaults.highlightStaminaBar, () -> config.highlightStaminaBar, val -> config.highlightStaminaBar = val))
                        .option(createBoolOption("flash_stamina_bar_when_full", defaults.flashStaminaBarWhenFull, () -> config.flashStaminaBarWhenFull, val -> config.flashStaminaBarWhenFull = val))
                        .option(createIntOption("flash_stamina_bar_at", defaults.flashStaminaBarAt, 0, 20, 1, () -> config.flashStaminaBarAt, val -> config.flashStaminaBarAt = val))
                        .build())
                .group(OptionGroup.createBuilder()
                        .name(Component.translatable("config.modest_meals.group.stamina_text"))
                        .option(createBoolOption("show_stamina_text", defaults.showStaminaText, () -> config.showStaminaText, val -> config.showStaminaText = val))
                        .option(createBoolOption("use_dynamic_stamina_color", defaults.useDynamicStaminaColor, () -> config.useDynamicStaminaColor, val -> config.useDynamicStaminaColor = val))
                        .option(createBoolOption("alt_stamina_show_on_active", defaults.altStaminaShowOnActive, () -> config.altStaminaShowOnActive, val -> config.altStaminaShowOnActive = val))
                        .option(createEnumOption("alt_stamina_corner", Corner.class, defaults.altStaminaCorner, () -> config.altStaminaCorner, val -> config.altStaminaCorner = val, Corner::getTitle))
                        .option(createIntOption("alt_stamina_offset_x", defaults.altStaminaOffsetX, -100, 100, 1, () -> config.altStaminaOffsetX, val -> config.altStaminaOffsetX = val))
                        .option(createIntOption("alt_stamina_offset_y", defaults.altStaminaOffsetY, -100, 100, 1, () -> config.altStaminaOffsetY, val -> config.altStaminaOffsetY = val))
                        .option(createBoolOption("alt_stamina_shadow", defaults.altStaminaShadow, () -> config.altStaminaShadow, val -> config.altStaminaShadow = val))
                        .option(createStringOption("alt_stamina_text", defaults.altStaminaText, () -> config.altStaminaText, val -> config.altStaminaText = val))
                        .build())
                .build();

        builder.category(mechanicsCategory);
        builder.category(hudCategory);

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
