package com.evandev.modest_meals.compat.emi;

//? if cultural_delights && emi {
import com.baisylia.culturaldelights.integration.emi.OvenRecipeHandler;
import com.baisylia.culturaldelights.screens.ModMenuTypes;
import com.evandev.modest_meals.compat.cultural_delights.OvenMealRecipe;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class OvenEmiSupport {

    public static void register(EmiRegistry registry, List<EmiStack> pool) {
        registry.addRecipeHandler(ModMenuTypes.OVEN_MENU.get(), new MealOvenHandler());

        Set<ResourceLocation> backing = new HashSet<>();
        for (RecipeHolder<?> holder : registry.getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof OvenMealRecipe oven)) {
                continue;
            }
            backing.add(holder.id());
            Optional<MealType> type = MealTypeManager.get(oven.mealTypeId())
                    .filter(found -> found.station().effective() == MealType.Station.OVEN);
            if (type.isEmpty() || type.get().resolveItem().isEmpty()) {
                continue;
            }
            registry.addRecipe(OvenMealEmiRecipe.of(type.get(), oven.getCookTime(), pool));
            if (MealEmiIngredients.hasDubious(type.get(), pool)) {
                registry.addRecipe(OvenMealEmiRecipe.dubiousOf(type.get(), oven.getCookTime(), pool));
            }
            List<MealType.ItemOverride> overrides = type.get().itemOverrides();
            for (int i = 0; i < overrides.size(); i++) {
                OvenMealEmiRecipe.overrideOf(type.get(), overrides.get(i), i, oven.getCookTime())
                        .ifPresent(registry::addRecipe);
            }
        }

        if (!backing.isEmpty()) {
            registry.removeRecipes(recipe -> backing.contains(recipe.getId()));
        }
    }

    private static class MealOvenHandler extends OvenRecipeHandler {
        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe instanceof OvenMealEmiRecipe;
        }
    }
}
//?}
