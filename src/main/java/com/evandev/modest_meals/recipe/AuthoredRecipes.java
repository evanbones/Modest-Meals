package com.evandev.modest_meals.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Lets a mod's own fixed recipe win over a meal type that would otherwise take the same inputs.
 */
public final class AuthoredRecipes {

    public static <I extends RecipeInput, T extends Recipe<I>> boolean anyMatch(Level level, RecipeType<T> type, I input,
                                                                              Class<?> mealRecipeClass) {
        if (level == null) {
            return false;
        }
        for (RecipeHolder<T> holder : level.getRecipeManager().getAllRecipesFor(type)) {
            T recipe = holder.value();
            if (!mealRecipeClass.isInstance(recipe) && recipe.matches(input, level)) {
                return true;
            }
        }
        return false;
    }
}
