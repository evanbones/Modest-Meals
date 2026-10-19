package com.evandev.modest_meals.recipe;

import com.evandev.modest_meals.food.ingredient.IngredientProfileManager;
import com.evandev.modest_meals.food.meal.MealAssembler;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MealCraftingRecipe extends CustomRecipe {

    public MealCraftingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findMatch(input).isPresent()
                && !AuthoredRecipes.anyMatch(level, RecipeType.CRAFTING, input, MealCraftingRecipe.class);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return findMatch(input)
                .flatMap(match -> MealAssembler.assemble(match.type(), match.ingredients(), match.base()))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MEAL_CRAFTING.get();
    }

    private Optional<Match> findMatch(CraftingInput input) {
        for (MealType type : MealTypeManager.craftingTable()) {
            Optional<Match> match = type.isShaped() ? matchShaped(type, input) : matchShapeless(type, input);
            if (match.isPresent()) {
                return match;
            }
        }
        return Optional.empty();
    }

    private Optional<Match> matchShapeless(MealType type, CraftingInput input) {
        List<ItemStack> present = new ArrayList<>();
        for (ItemStack stack : input.items()) {
            if (!stack.isEmpty()) {
                present.add(stack);
            }
        }

        Optional<MealType.Split> split = type.splitBase(present);
        if (split.isEmpty()) {
            return Optional.empty();
        }

        List<ItemStack> remaining = split.get().ingredients();
        if (remaining.isEmpty() || !remaining.stream().allMatch(IngredientProfileManager::isIngredient)) {
            return Optional.empty();
        }
        return type.accepts(remaining)
                ? Optional.of(new Match(type, remaining, split.get().base()))
                : Optional.empty();
    }

    private Optional<Match> matchShaped(MealType type, CraftingInput input) {
        MealType.Shape shape = type.shape().orElseThrow();
        int slackX = input.width() - shape.width();
        int slackY = input.height() - shape.height();

        for (int top = Math.min(0, slackY); top <= Math.max(0, slackY); top++) {
            for (int left = Math.min(0, slackX); left <= Math.max(0, slackX); left++) {
                for (boolean mirrored : new boolean[]{false, true}) {
                    Optional<MealType.Split> matched = matchAt(shape, input, left, top, mirrored);
                    if (matched.isPresent() && type.accepts(matched.get().ingredients())) {
                        return Optional.of(new Match(type, matched.get().ingredients(), matched.get().base()));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Optional<MealType.Split> matchAt(MealType.Shape shape, CraftingInput input,
                                             int left, int top, boolean mirrored) {
        List<ItemStack> ingredients = new ArrayList<>();
        List<ItemStack> base = new ArrayList<>();

        for (int row = Math.min(0, top); row < Math.max(input.height(), top + shape.height()); row++) {
            for (int column = Math.min(0, left); column < Math.max(input.width(), left + shape.width()); column++) {
                boolean insideGrid = row >= 0 && row < input.height() && column >= 0 && column < input.width();
                ItemStack stack = insideGrid ? input.getItem(column, row) : ItemStack.EMPTY;
                int patternRow = row - top;
                int patternColumn = column - left;

                boolean insidePattern = patternRow >= 0 && patternRow < shape.height()
                        && patternColumn >= 0 && patternColumn < shape.width();
                if (!insidePattern) {
                    if (!stack.isEmpty()) {
                        return Optional.empty();
                    }
                    continue;
                }

                int readColumn = mirrored ? shape.width() - 1 - patternColumn : patternColumn;
                char symbol = shape.symbolAt(patternRow, readColumn);

                if (symbol == ' ') {
                    if (!stack.isEmpty()) {
                        return Optional.empty();
                    }
                } else if (shape.isAny(symbol)) {
                    if (!stack.isEmpty()) {
                        if (!IngredientProfileManager.isIngredient(stack)) {
                            return Optional.empty();
                        }
                        ingredients.add(stack);
                    }
                } else {
                    Optional<Ingredient> pinned = shape.pinnedAt(symbol);
                    if (pinned.isEmpty() || !pinned.get().test(stack)) {
                        return Optional.empty();
                    }
                    base.add(stack);
                }
            }
        }

        return Optional.of(new MealType.Split(base, ingredients));
    }

    private record Match(MealType type, List<ItemStack> ingredients, List<ItemStack> base) {
    }
}
