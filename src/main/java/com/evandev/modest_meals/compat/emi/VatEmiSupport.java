package com.evandev.modest_meals.compat.emi;

import com.baisylia.culturaldelights.integration.emi.VatRecipeHandler;
import com.baisylia.culturaldelights.screens.ModMenuTypes;
import com.evandev.modest_meals.compat.cultural_delights.VatMealRecipe;
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

public final class VatEmiSupport {

    public static void register(EmiRegistry registry, List<EmiStack> pool) {
        registry.addRecipeHandler(ModMenuTypes.VAT_MENU.get(), new MealVatHandler());

        Set<ResourceLocation> backing = new HashSet<>();
        for (RecipeHolder<?> holder : registry.getRecipeManager().getRecipes()) {
            if (!(holder.value() instanceof VatMealRecipe vat)) {
                continue;
            }
            backing.add(holder.id());
            Optional<MealType> type = MealTypeManager.get(vat.mealTypeId());
            if (type.isEmpty() || type.get().resolveItem().isEmpty()) {
                continue;
            }
            registry.addRecipe(VatMealEmiRecipe.of(type.get(), vat.getCookTime(), pool));
            if (type.get().requiresSupportedIngredients()) {
                registry.addRecipe(VatMealEmiRecipe.dubiousOf(type.get(), vat.getCookTime(), pool));
            }
            List<MealType.ItemOverride> overrides = type.get().itemOverrides();
            for (int i = 0; i < overrides.size(); i++) {
                VatMealEmiRecipe.overrideOf(type.get(), overrides.get(i), i, vat.getCookTime())
                        .ifPresent(registry::addRecipe);
            }
        }

        if (!backing.isEmpty()) {
            registry.removeRecipes(recipe -> backing.contains(recipe.getId()));
        }
    }

    private static class MealVatHandler extends VatRecipeHandler {
        @Override
        public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe instanceof VatMealEmiRecipe;
        }
    }
}
