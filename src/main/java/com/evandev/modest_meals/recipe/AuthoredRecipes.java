package com.evandev.modest_meals.recipe;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
//? if >=26.1
/*import net.minecraft.server.level.ServerLevel;*/

public final class AuthoredRecipes {

    @SuppressWarnings("unchecked")
    public static <I extends RecipeInput, T extends Recipe<I>> boolean anyMatch(Level level, RecipeType<T> type, I input,
                                                                              Class<?> mealRecipeClass) {
        //? if >=26.1 {
        /*if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        for (RecipeHolder<?> holder : serverLevel.recipeAccess().getRecipes()) {
            if (holder.value().getType() == type && !mealRecipeClass.isInstance(holder.value())
                    && ((T) holder.value()).matches(input, level)) {
                return true;
            }
        }
        return false;
        *///?} else {
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
        //?}
    }
}
