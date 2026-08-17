package com.evandev.modest_meals.mixin.compat;

import com.evandev.modest_meals.trait.FoodCompounding;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "vectorwing.farmersdelight.common.crafting.DoughRecipe", remap = false)
public class DoughRecipeMixin {

    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), remap = false)
    private void mm$inheritDoughTraits(CraftingInput container, HolderLookup.Provider registryAccess, CallbackInfoReturnable<ItemStack> cir) {
        FoodCompounding.apply(cir.getReturnValue(), container, false);
    }
}
