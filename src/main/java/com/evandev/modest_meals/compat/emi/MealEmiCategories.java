package com.evandev.modest_meals.compat.emi;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.registry.ModItems;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;

public final class MealEmiCategories {

    public static final EmiRecipeCategory MEAL_CRAFTING = new EmiRecipeCategory(
            id("meal_crafting"), EmiStack.of(ModItems.SANDWICH.get()));

    public static final EmiRecipeCategory MEAL_INGREDIENT = new EmiRecipeCategory(
            id("meal_ingredient"), EmiStack.of(ModItems.SKEWER.get()));

    private MealEmiCategories() {
    }

    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
