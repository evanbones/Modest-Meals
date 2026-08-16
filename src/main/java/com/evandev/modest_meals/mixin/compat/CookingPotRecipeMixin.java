package com.evandev.modest_meals.mixin.compat;

import com.evandev.modest_meals.trait.FoodCompounding;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "vectorwing.farmersdelight.common.crafting.CookingPotRecipe", remap = false)
public class CookingPotRecipeMixin {

    @Inject(method = "assemble(Lnet/neoforged/neoforge/items/wrapper/RecipeWrapper;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), remap = false)
    private void mm$inheritCookingPotTraits(RecipeWrapper input, HolderLookup.Provider registries, CallbackInfoReturnable<ItemStack> cir) {
        FoodCompounding.apply(cir.getReturnValue(), input, true);
    }
}
