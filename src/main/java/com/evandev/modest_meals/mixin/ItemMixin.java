package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.food.FoodConsumption;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemMixin {

    @WrapOperation(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;canEat(Z)Z"
            )
    )
    private boolean mm$canEatHeldFood(
            Player player, boolean ignoreHunger, Operation<Boolean> original, @Local ItemStack itemStack
    ) {
        if (ignoreHunger) {
            return original.call(player, true);
        }
        return FoodConsumption.canConsume(player, itemStack);
    }
}
