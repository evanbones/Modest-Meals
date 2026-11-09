package com.evandev.modest_meals.component;

import com.evandev.modest_meals.platform.RegistrationProvider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

import java.util.function.Supplier;

public class ModDataComponents {
    private static final RegistrationProvider<DataComponentType<?>> DATA_COMPONENTS =
            RegistrationProvider.get(Registries.DATA_COMPONENT_TYPE);

    public static final Supplier<DataComponentType<MealContents>> MEAL_CONTENTS =
            DATA_COMPONENTS.register("meal_contents", () -> DataComponentType.<MealContents>builder()
                    .persistent(MealContents.CODEC)
                    .networkSynchronized(MealContents.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    public static void init() {
    }
}
