package com.evandev.modest_meals.recipe;

import com.evandev.modest_meals.food.meal.MealTypeManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class MealCraftingRecipe extends CustomRecipe {

    //? if >=26.1 {
    /*public MealCraftingRecipe() {
    }
    *///?} else {
    public MealCraftingRecipe(CraftingBookCategory category) {
        super(category);
    }
    //?}

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findMatch(input).isPresent()
                && !AuthoredRecipes.anyMatch(level, RecipeType.CRAFTING, input, MealCraftingRecipe.class);
    }

    @Override
    //? if >=26.1 {
    /*public ItemStack assemble(CraftingInput input) {
    *///?} else
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return findMatch(input)
                .flatMap(MealGridMatcher.Match::assemble)
                .orElse(ItemStack.EMPTY);
    }

    //? if >=26.1 {
    /*@Override
    public RecipeSerializer<MealCraftingRecipe> getSerializer() {
        return ModRecipes.MEAL_CRAFTING.get();
    }
    *///?} else {
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MEAL_CRAFTING.get();
    }
    //?}

    private Optional<MealGridMatcher.Match> findMatch(CraftingInput input) {
        return MealGridMatcher.findMatch(MealTypeManager.craftingTable(), input);
    }
}
