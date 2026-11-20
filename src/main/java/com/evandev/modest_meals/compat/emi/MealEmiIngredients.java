package com.evandev.modest_meals.compat.emi;

//? if emi {
import com.evandev.modest_meals.compat.viewer.MealViewerIngredients;
import com.evandev.modest_meals.food.meal.MealType;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class MealEmiIngredients {

    public static List<EmiStack> pool() {
        return MealViewerIngredients.pool().stream().map(EmiStack::of).toList();
    }

    public static List<EmiStack> poolFor(MealType type, List<EmiStack> defaultPool) {
        return defaultPool.stream()
                .filter(stack -> MealViewerIngredients.fits(type, stack.getItemStack()))
                .toList();
    }

    public static List<EmiStack> unsupportedPoolFor(MealType type, List<EmiStack> defaultPool) {
        List<ItemStack> stacks = defaultPool.stream().map(EmiStack::getItemStack).toList();
        return MealViewerIngredients.unsupportedPool(type, stacks).stream().map(EmiStack::of).toList();
    }

    public static boolean hasDubious(MealType type, List<EmiStack> defaultPool) {
        return !unsupportedPoolFor(type, defaultPool).isEmpty();
    }
}
//?}
