package com.evandev.modest_meals.compat.rrv;

//? if rrv && cultural_delights {
/*import cc.cassian.rrv.api.recipe.ItemView;
import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.client.recipe.ClientRecipeManager;
import com.baisylia.culturaldelights.integration.rrv.type.VatClientRecipeType;
import com.baisylia.culturaldelights.recipes.ModRecipes;
import com.baisylia.culturaldelights.recipes.VatRecipe;
import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.compat.cultural_delights.VatMealRecipe;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public final class VatRrvSupport {

    private static final ResourceLocation HIDE_BACKING_RECIPES = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "hide_vat_meal_recipes");

    public static void hideBackingRecipes() {
        ClientRecipeSynchronizedEvent.EVENT.addPhaseOrdering(HIDE_BACKING_RECIPES, Event.DEFAULT_PHASE);
        ClientRecipeSynchronizedEvent.EVENT.register(HIDE_BACKING_RECIPES, (client, recipes) ->
                recipes.recipes().forEach(holder -> {
                    if (holder.value() instanceof VatMealRecipe) {
                        ItemView.excludeRecipe(VatClientRecipeType.INSTANCE.getId(), holder.id().identifier());
                    }
                }));
    }

    public static void provide(List<ReliableClientRecipe> recipes, List<ItemStack> pool) {
        for (RecipeHolder<VatRecipe> holder : ClientRecipeManager.INSTANCE.getRecipesForType(ModRecipes.AGING_TYPE.get())) {
            if (holder.value() instanceof VatMealRecipe meal) {
                recipes.addAll(MealVatRrvRecipe.of(meal, pool));
            }
        }
    }
}
*///?}
