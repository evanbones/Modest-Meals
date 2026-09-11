package com.evandev.modest_meals.compat.emi;

import com.evandev.modest_meals.compat.farmers_delight.MealCookingRecipe;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.utility.RecipeUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class CookingPotEmiSupport {

    private static final ResourceLocation SIMPLIFIED = RecipeUtils.FDLocation("textures/gui/emi/simplified.png");
    private static final EmiStack COOKING_POT = EmiStack.of(ModItems.COOKING_POT.get());

    public static final EmiRecipeCategory MEAL_COOKING = new EmiRecipeCategory(
            MealEmiCategories.id("meal_cooking"), COOKING_POT, simplifiedCookingIcon());

    private CookingPotEmiSupport() {
    }

    private static EmiRenderable simplifiedCookingIcon() {
        return (draw, x, y, delta) -> draw.blit(SIMPLIFIED, x, y, 0, 0, 16, 16, 48, 16);
    }

    public static void register(EmiRegistry registry, List<EmiStack> pool) {
        registry.addCategory(MEAL_COOKING);
        registry.addWorkstation(MEAL_COOKING, COOKING_POT);

        Set<ResourceLocation> backing = new HashSet<>();
        for (RecipeHolder<?> holder : registry.getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof MealCookingRecipe cooking)) {
                continue;
            }
            Optional<MealType> type = MealTypeManager.get(cooking.mealTypeId());
            if (type.isEmpty() || type.get().resolveItem().isEmpty()) {
                continue;
            }
            backing.add(holder.id());
            registry.addRecipe(CookingMealEmiRecipe.of(
                    type.get(), cooking.getCookTime(), cooking.getExperience(), pool));
            if (type.get().requiresSupportedIngredients()) {
                registry.addRecipe(CookingMealEmiRecipe.dubiousOf(
                        type.get(), cooking.getCookTime(), cooking.getExperience(), pool));
            }
        }

        if (!backing.isEmpty()) {
            registry.removeRecipes(recipe -> backing.contains(recipe.getId()));
        }
    }
}
