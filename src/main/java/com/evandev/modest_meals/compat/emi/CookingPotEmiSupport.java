package com.evandev.modest_meals.compat.emi;

import com.evandev.modest_meals.compat.farmers_delight.MealCookingRecipe;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import vectorwing.farmersdelight.common.registry.ModMenuTypes;
import vectorwing.farmersdelight.integration.emi.handler.CookingPotEmiRecipeHandler;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class CookingPotEmiSupport {

    public static void register(EmiRegistry registry, List<EmiStack> pool) {
        registry.addRecipeHandler(ModMenuTypes.COOKING_POT.get(), new MealCookingPotHandler());

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
            List<MealType.ItemOverride> overrides = type.get().itemOverrides();
            for (int i = 0; i < overrides.size(); i++) {
                CookingMealEmiRecipe.overrideOf(type.get(), overrides.get(i), i,
                        cooking.getCookTime(), cooking.getExperience()).ifPresent(registry::addRecipe);
            }
        }

        if (!backing.isEmpty()) {
            registry.removeRecipes(recipe -> backing.contains(recipe.getId()));
        }
    }

    private static class MealCookingPotHandler extends CookingPotEmiRecipeHandler {
        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe instanceof CookingMealEmiRecipe;
        }
    }
}
