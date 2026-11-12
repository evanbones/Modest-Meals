package com.evandev.modest_meals.compat.cultural_delights;

import com.evandev.modest_meals.platform.Platform;
import com.evandev.modest_meals.platform.RegistrationProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
//? if cultural_delights && fabric && >=26.1
/*import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;*/

import java.util.function.Supplier;

public class CulturalDelightsCompat {
    public static final String MOD_ID = "culturaldelights";

    //? if cultural_delights
    public static Supplier<RecipeSerializer<VatMealRecipe>> VAT_MEAL_SERIALIZER;

    public static boolean isLoaded() {
        //? if cultural_delights {
        return Platform.isModLoaded(MOD_ID);
        //?} else
        /*return false;*/
    }

    public static void init() {
        if (!isLoaded()) {
            return;
        }
        //? if cultural_delights {
        RegistrationProvider<RecipeSerializer<?>> serializers = RegistrationProvider.get(Registries.RECIPE_SERIALIZER);
        VAT_MEAL_SERIALIZER = serializers.register("meal_vat", VatMealRecipe::serializer);
        //?}
        //? if cultural_delights && fabric && >=26.1
        /*RecipeSynchronization.synchronizeRecipeSerializer(VAT_MEAL_SERIALIZER.get());*/
    }
}
