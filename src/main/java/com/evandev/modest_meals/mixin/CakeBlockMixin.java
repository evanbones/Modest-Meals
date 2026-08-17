package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.food.FoodConsumption;
import com.evandev.modest_meals.trait.FoodTraitManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CakeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
        FoodTraitManager.applyAll(player, mm$cakeStack(), 1.0F / (CakeBlock.MAX_BITES + 1));

        if (!ModConfig.get().disableHunger) {
            original.call(foodData, foodLevelModifier, saturationLevelModifier);
        }
    }

    @WrapOperation(
            method = "eat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;canEat(Z)Z"
            )
    )
    private static boolean mm$canEatCake(
            Player player, boolean ignoreHunger, Operation<Boolean> original
    ) {
        if (ignoreHunger) {
            return original.call(player, true);
        }
        return FoodConsumption.canConsume(player, mm$cakeStack());
    }

    @Unique
    private static ItemStack mm$cakeStack() {
        return Items.CAKE.getDefaultInstance();
    }
}
