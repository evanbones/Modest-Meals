package com.evandev.modest_meals.recipe;

import com.evandev.modest_meals.platform.RegistrationProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

//? if >=26.1 {
/*import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
*///?} else
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public class ModRecipes {
    private static final RegistrationProvider<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            RegistrationProvider.get(Registries.RECIPE_SERIALIZER);

    public static final Supplier<RecipeSerializer<MealCraftingRecipe>> MEAL_CRAFTING =
            RECIPE_SERIALIZERS.<RecipeSerializer<MealCraftingRecipe>>register("meal_crafting", ModRecipes::mealCraftingSerializer);

    private static RecipeSerializer<MealCraftingRecipe> mealCraftingSerializer() {
        //? if >=26.1 {
        /*MealCraftingRecipe instance = new MealCraftingRecipe();
        return new RecipeSerializer<>(MapCodec.unit(instance), StreamCodec.unit(instance));
        *///?} else
        return new SimpleCraftingRecipeSerializer<>(MealCraftingRecipe::new);
    }

    public static void init() {
    }
}
