package com.evandev.modest_meals.compat.cultural_delights;

import com.evandev.modest_meals.platform.Platform;
import com.evandev.modest_meals.platform.RegistrationProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

public class CulturalDelightsCompat {
    public static final String MOD_ID = "culturaldelights";

    //? if neoforge_legacy
    //public static Supplier<VatMealRecipe.Serializer> VAT_MEAL_SERIALIZER;

    public static boolean isLoaded() {
        return Platform.isModLoaded(MOD_ID);
    }

    public static void init() {
        //? if neoforge_legacy {
        /*if (!isLoaded()) {
            return;
        }
        RegistrationProvider<RecipeSerializer<?>> serializers = RegistrationProvider.get(Registries.RECIPE_SERIALIZER);
        VAT_MEAL_SERIALIZER = serializers.register("meal_vat", VatMealRecipe.Serializer::new);
        *///?}
    }
}
