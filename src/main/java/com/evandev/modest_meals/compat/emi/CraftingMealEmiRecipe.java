package com.evandev.modest_meals.compat.emi;

//? if emi {
import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.item.MealItem;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public class CraftingMealEmiRecipe extends MealEmiRecipe {

    private final boolean shapeless;

    private CraftingMealEmiRecipe(MealType type, EmiIngredient any, List<EmiIngredient> slots) {
        this(type, any, slots, syntheticId("meal_crafting", type),
                type.resolveItem().map(item -> EmiStack.of(item, type.servings())).orElse(EmiStack.EMPTY));
    }

    private CraftingMealEmiRecipe(MealType type, EmiIngredient any, List<EmiIngredient> slots, ResourceLocation id, EmiStack output) {
        super(VanillaEmiRecipeCategories.CRAFTING, id, type, any, slots, output);
        this.shapeless = !type.isShaped();
    }

    public static CraftingMealEmiRecipe of(MealType type, List<EmiStack> pool) {
        List<EmiStack> typePool = MealEmiIngredients.poolFor(type, pool);
        EmiIngredient any = typePool.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(typePool);
        return new CraftingMealEmiRecipe(type, any, gridSlots(type, any));
    }

    public static CraftingMealEmiRecipe dubiousOf(MealType type, List<EmiStack> pool) {
        List<EmiStack> unsupported = MealEmiIngredients.unsupportedPoolFor(type, pool);
        EmiIngredient any = unsupported.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(unsupported);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                "/meal_crafting_dubious/" + type.id().getNamespace() + "/" + type.id().getPath());
        EmiStack output = type.resolveItem().map(item -> EmiStack.of(MealItem.makeDubious(item))).orElse(EmiStack.EMPTY);
        return new CraftingMealEmiRecipe(type, any, gridSlots(type, any), id, output);
    }

    public static Optional<CraftingMealEmiRecipe> overrideOf(MealType type, MealType.ItemOverride override, int index) {
        EmiStack output = overrideOutput(override);
        if (output.isEmpty()) {
            return Optional.empty();
        }
        return overrideFillings(override).map(fillings -> new CraftingMealEmiRecipe(type, null,
                gridOverrideSlots(type, fillings, override.base()),
                overrideId("meal_crafting", type, index), output));
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
        for (int i = 0; i < GRID_SLOTS; i++) {
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

        widgets.addSlot(output, 92, 14).large(true).recipeContext(this);
    }
}
//?}
