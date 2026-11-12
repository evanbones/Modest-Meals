package com.evandev.modest_meals.compat.rrv;

//? if rrv {
/*import cc.cassian.rrv.api.ReliableRecipeViewerClientPlugin;
import cc.cassian.rrv.api.recipe.ItemView;
import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import com.evandev.modest_meals.compat.viewer.MealViewerIngredients;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import net.minecraft.world.item.ItemStack;

import java.util.List;
*///?}
//? if rrv && farmers_delight
/*import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightCompat;*/
//? if rrv && cultural_delights
/*import com.evandev.modest_meals.compat.cultural_delights.CulturalDelightsCompat;*/
//? if rrv {

/*public class ModestMealsRrvPlugin implements ReliableRecipeViewerClientPlugin {

    @Override
    public void onIntegrationInitialize() {
        ItemView.addClientRecipeProvider(ModestMealsRrvPlugin::provide);
*///?}
        //? if rrv && cultural_delights {
        /*if (CulturalDelightsCompat.isLoaded()) {
            VatRrvSupport.hideBackingRecipes();
        }
        *///?}
//? if rrv {
    /*}

    private static void provide(List<ReliableClientRecipe> recipes) {
        List<ItemStack> pool = MealViewerIngredients.pool();
        if (pool.isEmpty()) {
            return;
        }
        for (MealType type : MealTypeManager.craftingTable()) {
            recipes.addAll(MealCraftingRrvRecipe.of(type, pool));
        }
*///?}
        //? if rrv && farmers_delight {
        /*if (FarmersDelightCompat.isLoaded()) {
            for (MealType type : MealTypeManager.cookingPot()) {
                recipes.addAll(MealCookingRrvRecipe.of(type, pool));
            }
        }
        *///?}
        //? if rrv && cultural_delights {
        /*if (CulturalDelightsCompat.isLoaded()) {
            VatRrvSupport.provide(recipes, pool);
        }
        *///?}
//? if rrv {
    /*}
}
*///?}
