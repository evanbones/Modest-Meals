package com.evandev.modest_meals.fabric.mixin.compat;

//? if farmers_delight && >=26.1 {
/*import com.evandev.modest_meals.compat.farmers_delight.MealCuttingRecipe;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;

import java.util.List;

@Pseudo
@Mixin(targets = "vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity")
public abstract class FarmersDelightCuttingBoardMixin {

    @WrapOperation(
            method = "lambda$processStoredItemUsingTool$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lvectorwing/farmersdelight/common/crafting/CuttingBoardRecipe;rollResults(Lnet/minecraft/util/RandomSource;I)Ljava/util/List;"
            )
    )
    private List<ItemStack> mm$cutMealPortions(CuttingBoardRecipe recipe, RandomSource random, int fortune,
                                                Operation<List<ItemStack>> original) {
        if (recipe instanceof MealCuttingRecipe meal) {
            return meal.cut(((CuttingBoardBlockEntity) (Object) this).getStoredItem());
        }
        return original.call(recipe, random, fortune);
    }
}
*///?}
