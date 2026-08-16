package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.trait.FoodCompounding;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractCookingRecipe.class)
public class AbstractCookingRecipeMixin {

    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/SingleRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    private void mm$inheritFoodTraits(SingleRecipeInput input, HolderLookup.Provider registries, CallbackInfoReturnable<ItemStack> cir) {
        FoodCompounding.apply(cir.getReturnValue(), input, true);
    }
}
