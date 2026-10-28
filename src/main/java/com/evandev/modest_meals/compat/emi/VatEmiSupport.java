package com.evandev.modest_meals.compat.emi;

import com.evandev.modest_meals.compat.cultural_delights.CulturalDelightsCompat;
import com.evandev.modest_meals.compat.cultural_delights.VatMealRecipe;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class VatEmiSupport {

    private static final EmiStack VAT = EmiStack.of(BuiltInRegistries.ITEM.get(
            ResourceLocation.fromNamespaceAndPath(CulturalDelightsCompat.MOD_ID, "vat")));

    public static final EmiRecipeCategory MEAL_CHILLING = new EmiRecipeCategory(
            MealEmiCategories.id("meal_chilling"), VAT);

    public static void register(EmiRegistry registry, List<EmiStack> pool) {
        registry.addCategory(MEAL_CHILLING);
        registry.addWorkstation(MEAL_CHILLING, VAT);

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
}
