package com.evandev.modest_meals.component;

import com.evandev.modest_meals.Constants;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Constants.MOD_ID);

    public static final Supplier<DataComponentType<MealContents>> MEAL_CONTENTS =
            DATA_COMPONENTS.registerComponentType("meal_contents", builder -> builder
                    .persistent(MealContents.CODEC)
                    .networkSynchronized(MealContents.STREAM_CODEC)
                    .cacheEncoding());

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }
}
