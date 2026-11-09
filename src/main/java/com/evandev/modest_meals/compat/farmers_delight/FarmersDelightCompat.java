package com.evandev.modest_meals.compat.farmers_delight;

import com.evandev.modest_meals.platform.Platform;
import com.evandev.modest_meals.platform.RegistrationProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

public class FarmersDelightCompat {
    public static final String MOD_ID = "farmersdelight";

    //? if farmers_delight {
    public static Supplier<RecipeSerializer<MealCookingRecipe>> MEAL_COOKING_SERIALIZER;
    public static Supplier<RecipeSerializer<MealCuttingRecipe>> MEAL_CUTTING_SERIALIZER;
    //?}

    public static boolean isLoaded() {
        //? if farmers_delight {
        return Platform.isModLoaded(MOD_ID);
        //?} else
        /*return false;*/
    }

    public static void init() {
        //? if farmers_delight {
        if (!isLoaded()) {
            return;
        }
        RegistrationProvider<RecipeSerializer<?>> serializers = RegistrationProvider.get(Registries.RECIPE_SERIALIZER);
        MEAL_COOKING_SERIALIZER = serializers.register("meal_cooking", MealCookingRecipe::serializer);
        MEAL_CUTTING_SERIALIZER = serializers.register("meal_cutting", MealCuttingRecipe::serializer);
        //?}
    }
}
