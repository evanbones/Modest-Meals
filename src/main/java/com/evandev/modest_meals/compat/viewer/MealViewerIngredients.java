package com.evandev.modest_meals.compat.viewer;

import com.evandev.modest_meals.food.ingredient.IngredientProfile;
import com.evandev.modest_meals.food.ingredient.IngredientProfileManager;
import com.evandev.modest_meals.food.ingredient.MealIngredientManager;
import com.evandev.modest_meals.food.meal.MealType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class MealViewerIngredients {

    public static List<ItemStack> pool() {
        return BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new)
                .filter(IngredientProfileManager::isIngredient)
                .sorted(Comparator.comparingInt(MealViewerIngredients::interest))
                .toList();
    }

    public static boolean fits(MealType type, ItemStack stack) {
        boolean allowed = type.rules().allowed().map(matcher -> matcher.test(stack)).orElse(true);
        return allowed && (!type.requiresSupportedIngredients() || MealIngredientManager.isSupported(type.id(), stack));
    }

    public static boolean isUnsupported(MealType type, ItemStack stack) {
        return !MealIngredientManager.isSupported(type.id(), stack);
    }

    public static ItemStack unsupportedFallback() {
        return new ItemStack(Items.ROTTEN_FLESH);
    }

    private static int interest(ItemStack stack) {
        Optional<IngredientProfile> authored = IngredientProfileManager.authored(stack);
        if (authored.filter(IngredientProfile::hasEffect).isPresent()) {
            return 0;
        }
        return authored.isPresent() ? 1 : 2;
    }
}
