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
        return type.rules().permits(stack)
                && (!type.requiresSupportedIngredients() || MealIngredientManager.isSupported(type.id(), stack));
    }

    public static boolean isUnsupported(MealType type, ItemStack stack) {
        return type.rules().permits(stack) && !MealIngredientManager.isSupported(type.id(), stack);
    }

    public static List<ItemStack> unsupportedPool(MealType type, List<ItemStack> pool) {
        if (!type.requiresSupportedIngredients()) {
            return List.of();
        }
        List<ItemStack> unsupported = pool.stream().filter(stack -> isUnsupported(type, stack)).toList();
        if (!unsupported.isEmpty()) {
            return unsupported;
        }
        ItemStack fallback = new ItemStack(Items.ROTTEN_FLESH);
        return isUnsupported(type, fallback) ? List.of(fallback) : List.of();
    }

    private static int interest(ItemStack stack) {
        Optional<IngredientProfile> authored = IngredientProfileManager.authored(stack);
        if (authored.filter(IngredientProfile::hasEffect).isPresent()) {
            return 0;
        }
        return authored.isPresent() ? 1 : 2;
    }
}
