package com.evandev.modest_meals.component;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.trait.FoodTraitsData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(
            Registries.DATA_COMPONENT_TYPE, Constants.MOD_ID
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FoodTraitsData>> FOOD_TRAITS = DATA_COMPONENTS.register(
            "food_traits",
            () -> DataComponentType.<FoodTraitsData>builder()
                    .persistent(FoodTraitsData.CODEC)
                    .networkSynchronized(FoodTraitsData.STREAM_CODEC)
                    .build()
    );

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }
}
