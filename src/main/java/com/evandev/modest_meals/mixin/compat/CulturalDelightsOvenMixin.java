package com.evandev.modest_meals.mixin.compat;

//? if cultural_delights && <26.1 {
import com.baisylia.culturaldelights.block.entity.custom.OvenBlockEntity;
import com.baisylia.culturaldelights.recipes.OvenShapedRecipe;
import com.evandev.modest_meals.compat.cultural_delights.OvenMealRecipe;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
//?}
//? if neoforge_legacy
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
//? if cultural_delights && fabric && <26.1
//import vectorwing.farmersdelight.refabricated.inventory.RecipeWrapper;
//? if cultural_delights && <26.1 {

@Pseudo
//?}
//? if neoforge_legacy
@Mixin(targets = "com.baisylia.culturaldelights.block.entity.custom.OvenBlockEntity", remap = false)
//? if cultural_delights && fabric && <26.1
//@Mixin(targets = "com.baisylia.culturaldelights.block.entity.custom.OvenBlockEntity")
//? if cultural_delights && <26.1 {
public abstract class CulturalDelightsOvenMixin {

    @WrapOperation(
            method = {"hasRecipe", "craftItem"},
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/baisylia/culturaldelights/recipes/OvenShapedRecipe;getResultItem(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private static ItemStack mm$assembleMeal(OvenShapedRecipe recipe, HolderLookup.Provider registries,
                                             Operation<ItemStack> original,
                                             @Local(argsOnly = true) OvenBlockEntity oven) {
        if (recipe instanceof OvenMealRecipe meal) {
            return meal.assemble(new RecipeWrapper(oven.getItemHandler()), registries);
        }
        return original.call(recipe, registries);
    }
}
//?}
