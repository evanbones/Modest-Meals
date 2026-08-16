package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.food.EdibleBlockFoods;
import com.evandev.modest_meals.regen.HealthRegenHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CakeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CakeBlock.class)
public class CakeBlockMixin {

    @WrapOperation(
            method = "eat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"
            )
    )
    private static void mm$eatCake(
            FoodData foodData, int foodLevelModifier, float saturationLevelModifier, Operation<Void> original,
            @Local(argsOnly = true) Player player
    ) {
        boolean didConsume = HealthRegenHelper.get(player).eat(Items.CAKE.getDefaultInstance(), EdibleBlockFoods.CAKE_PROPERTIES);
        if (!didConsume) {
            original.call(foodData, foodLevelModifier, saturationLevelModifier);
        }
    }
}
