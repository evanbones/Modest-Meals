package com.evandev.modest_meals.client;

import com.evandev.modest_meals.config.ModConfig;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClientConfigScreen {

    public static Screen create(Screen parent) {
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.modest_meals.title"))
                .save(ModConfig::save);


        return builder.build().generateScreen(parent);
    }

    private static Option<Boolean> createBoolOption(String name, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return createBoolOption(name, defaultValue, true, getter, setter);
    }

    private static Option<Boolean> createBoolOption(String name, boolean defaultValue, boolean available,
                                                    Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(describe(name, available))
                .available(available)
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    private static Option<Float> createFloatOption(String name, float defaultValue, float min, float max, float step, Supplier<Float> getter, Consumer<Float> setter) {
        return createFloatOption(name, defaultValue, min, max, step, true, getter, setter);
    }

    private static Option<Float> createFloatOption(String name, float defaultValue, float min, float max, float step,
                                                   boolean available, Supplier<Float> getter, Consumer<Float> setter) {
        return Option.<Float>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(describe(name, available))
                .available(available)
                .binding(defaultValue, getter, setter)
                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(min, max).step(step))
                .build();
    }

    private static OptionDescription describe(String name, boolean available) {
        MutableComponent tooltip = Component.translatable("config.modest_meals.option." + name + ".tooltip");
        if (available) {
            return OptionDescription.of(tooltip);
        }
        return OptionDescription.of(tooltip
                .append(CommonComponents.NEW_LINE)
                .append(CommonComponents.NEW_LINE)
                .append(Component.translatable("config.modest_meals.requires_create").withStyle(ChatFormatting.GRAY)));
    }

    private static Option<Integer> createIntOption(String name, int defaultValue, int min, int max, int step, Supplier<Integer> getter, Consumer<Integer> setter) {
        return Option.<Integer>createBuilder()
                .name(Component.translatable("config.modest_meals.option." + name))
                .description(describe(name, true))
                .binding(defaultValue, getter, setter)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(min, max).step(step))
                .build();
    }
}
