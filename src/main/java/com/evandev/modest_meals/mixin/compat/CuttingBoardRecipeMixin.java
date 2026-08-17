package com.evandev.modest_meals.mixin.compat;

import com.evandev.modest_meals.trait.FoodCompounding;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe", remap = false)
public class CuttingBoardRecipeMixin {

    @Inject(method = "rollResults(Lnet/minecraft/util/RandomSource;ILnet/neoforged/neoforge/items/wrapper/RecipeWrapper;)Ljava/util/List;", at = @At("RETURN"), remap = false)
    private void mm$inheritCuttingBoardTraits(RandomSource random, int fortuneLevel, RecipeWrapper inventory, CallbackInfoReturnable<List<ItemStack>> cir) {
        List<ItemStack> results = cir.getReturnValue();
        if (results == null || results.isEmpty() || inventory == null) return;

        List<ItemStack> inputs = List.of(inventory.getItem(0));
        for (ItemStack result : results) {
            FoodCompounding.apply(result, inputs, false);
        }
    }
}
