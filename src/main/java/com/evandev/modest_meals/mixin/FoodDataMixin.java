package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.food.FoodConsumption;
import com.evandev.modest_meals.food.FoodDataOwner;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin implements FoodDataOwner {
    @Unique
    @Nullable
    private Player mm$owner = null;

    @Override
    public @Nullable Player mm$getOwner() {
        return this.mm$owner;
    }

    @Override
    public void mm$setOwner(Player player) {
        this.mm$owner = player;
    }

    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    public void mm$disableHunger(Player player, CallbackInfo callback) {
        if (mm$owner == null) {
            mm$owner = player;
        }
        if (ModConfig.get().disableHunger) {
            callback.cancel();
        }
    }

    @WrapMethod(method = "eat(IF)V")
    private void mm$eatRawFood(int nutrition, float saturationModifier, Operation<Void> original) {
        mm$feedFromBlock(nutrition);
        original.call(nutrition, saturationModifier);
    }

    @WrapMethod(method = "eat(Lnet/minecraft/world/food/FoodProperties;)V")
    private void mm$eatFoodProperties(FoodProperties foodProperties, Operation<Void> original) {
        mm$feedFromBlock(foodProperties.nutrition());
        original.call(foodProperties);
    }

    @Unique
    private void mm$feedFromBlock(int nutrition) {
        if (mm$owner != null) {
            FoodConsumption.consumeFromBlock(mm$owner, nutrition);
        }
    }

    @WrapMethod(method = "add")
    private void mm$consumeFoodProperties(int foodLevel, float saturationLevel, Operation<Void> original) {
        if (ModConfig.get().disableHunger) {
            return;
        }
        original.call(foodLevel, saturationLevel);
    }
}
