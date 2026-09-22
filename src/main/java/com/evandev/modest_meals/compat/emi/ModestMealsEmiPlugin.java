package com.evandev.modest_meals.compat.emi;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightCompat;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.Items;

import java.util.List;

@EmiEntrypoint
public class ModestMealsEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        List<EmiStack> pool = MealEmiIngredients.pool();
        if (pool.isEmpty()) {
            return;
        }

        registry.addCategory(MealEmiCategories.MEAL_CRAFTING);
        registry.addWorkstation(MealEmiCategories.MEAL_CRAFTING, EmiStack.of(Items.CRAFTING_TABLE));
        for (MealType type : MealTypeManager.craftingTable()) {
            if (type.resolveItem().isPresent()) {
                registry.addRecipe(CraftingMealEmiRecipe.of(type, pool));
                if (type.requiresSupportedIngredients()) {
                    registry.addRecipe(CraftingMealEmiRecipe.dubiousOf(type, pool));
                }
            }
        }

        if (FarmersDelightCompat.isLoaded()) {
            CookingPotEmiSupport.register(registry, pool);
        }

        Constants.LOG.debug("Registered EMI recipes for Modest Meals ({} pool ingredients)", pool.size());
    }
}
