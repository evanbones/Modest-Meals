package com.evandev.modest_meals.compat.cultural_delights;

import com.evandev.modest_meals.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CulturalDelightsCompat {
    public static final String MOD_ID = "culturaldelights";

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Constants.MOD_ID);

    public static Supplier<VatMealRecipe.Serializer> VAT_MEAL_SERIALIZER;

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void register(IEventBus modBus) {
        if (!isLoaded()) {
            return;
        }
        VAT_MEAL_SERIALIZER = RECIPE_SERIALIZERS.register("meal_vat", VatMealRecipe.Serializer::new);
        RECIPE_SERIALIZERS.register(modBus);
    }
}
