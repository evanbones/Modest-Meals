package com.evandev.modest_meals.compat.emi;

import com.evandev.modest_meals.food.meal.MealType;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CraftingMealEmiRecipe extends MealEmiRecipe {

    private static final int GRID = 3;
    private static final int SLOTS = GRID * GRID;

    private final boolean shapeless;

    private CraftingMealEmiRecipe(MealType type, EmiIngredient any, List<EmiIngredient> slots) {
        super(MealEmiCategories.MEAL_CRAFTING, syntheticId("meal_crafting", type), type, any, slots);
        this.shapeless = !type.isShaped();
    }

    public static CraftingMealEmiRecipe of(MealType type, List<EmiStack> pool) {
        EmiIngredient any = pool.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(pool);
        return new CraftingMealEmiRecipe(type, any,
                type.isShaped() ? shapedSlots(type, any) : flatSlots(type, any, SLOTS));
    }

    private static List<EmiIngredient> shapedSlots(MealType type, EmiIngredient any) {
        MealType.Shape shape = type.shape().orElseThrow();
        List<EmiIngredient> laid = new ArrayList<>(Collections.nCopies(SLOTS, (EmiIngredient) EmiStack.EMPTY));
        for (int row = 0; row < Math.min(GRID, shape.height()); row++) {
            for (int column = 0; column < Math.min(GRID, shape.width()); column++) {
                char symbol = shape.symbolAt(row, column);
                if (symbol != ' ') {
                    laid.set(row * GRID + column, keyIngredient(shape, symbol, any));
                }
            }
        }
        return laid;
    }

    @Override
    public int getDisplayWidth() {
        return 118;
    }

    @Override
    public int getDisplayHeight() {
        return 54;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int i = 0; i < SLOTS; i++) {
            EmiIngredient ingredient = slots.get(i);
            SlotWidget slot = widgets.addSlot(ingredient, i % GRID * 18, i / GRID * 18);
            if (ingredient == any) {
                decorateFreeSlot(slot);
            }
        }

        widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 18);
        if (shapeless) {
            widgets.addTexture(EmiTexture.SHAPELESS, 97, 0);
        }

        SlotWidget result = widgets.addSlot(output, 92, 14).large(true).recipeContext(this);
        result.appendTooltip(ingredientCountLine());
        decorateOutput(result);
    }
}
