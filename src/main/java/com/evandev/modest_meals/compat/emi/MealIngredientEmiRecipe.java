package com.evandev.modest_meals.compat.emi;

import com.evandev.modest_meals.Constants;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class MealIngredientEmiRecipe implements EmiRecipe {

    private static final int LINE_HEIGHT = 11;
    private static final int TEXT_LEFT = 22;

    private final EmiStack stack;
    private final ResourceLocation id;
    private final List<Component> lines;

    public MealIngredientEmiRecipe(EmiStack stack) {
        this.stack = stack;
        this.id = syntheticId(stack);
        this.lines = MealEmiIngredients.contributionLines(stack.getItemStack());
    }

    private static ResourceLocation syntheticId(EmiStack stack) {
        ResourceLocation item = stack.getId();
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                "/meal_ingredient/" + item.getNamespace() + "/" + item.getPath());
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return MealEmiCategories.MEAL_INGREDIENT;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(stack);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(stack);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public boolean hideCraftable() {
        return true;
    }

    @Override
    public int getDisplayWidth() {
        return 134;
    }

    @Override
    public int getDisplayHeight() {
        return Math.max(18, lines.size() * LINE_HEIGHT);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(stack, 0, 0).recipeContext(this);
        for (int i = 0; i < lines.size(); i++) {
            widgets.addText(lines.get(i), TEXT_LEFT, i * LINE_HEIGHT + 1, -1, true);
        }
    }
}
