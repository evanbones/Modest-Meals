package com.evandev.modest_meals.compat.emi;

import com.evandev.modest_meals.food.ingredient.IngredientProfile;
import com.evandev.modest_meals.food.ingredient.IngredientProfileManager;
import com.evandev.modest_meals.food.ingredient.MealIngredientManager;
import com.evandev.modest_meals.food.meal.MealType;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class MealEmiIngredients {

    private MealEmiIngredients() {
    }

    public static List<EmiStack> pool() {
        List<ItemStack> accepted = BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new)
                .filter(IngredientProfileManager::isIngredient)
                .toList();

        return accepted.stream()
                .sorted(Comparator.comparingInt(MealEmiIngredients::interest))
                .map(EmiStack::of)
                .toList();
    }

    public static List<EmiStack> poolFor(MealType type, List<EmiStack> defaultPool) {
        if (!type.requiresSupportedIngredients()) {
            return defaultPool;
        }
        return defaultPool.stream()
                .filter(stack -> MealIngredientManager.isSupported(type.id(), stack.getItemStack()))
                .toList();
    }

    public static List<EmiStack> unsupportedPoolFor(MealType type, List<EmiStack> defaultPool) {
        List<EmiStack> list = defaultPool.stream()
                .filter(stack -> !MealIngredientManager.isSupported(type.id(), stack.getItemStack()))
                .toList();
        if (list.isEmpty()) {
            return List.of(EmiStack.of(Items.ROTTEN_FLESH));
        }
        return list;
    }

    private static int interest(ItemStack stack) {
        Optional<IngredientProfile> authored = IngredientProfileManager.authored(stack);
        if (authored.filter(IngredientProfile::hasEffect).isPresent()) {
            return 0;
        }
        return authored.isPresent() ? 1 : 2;
    }
}

