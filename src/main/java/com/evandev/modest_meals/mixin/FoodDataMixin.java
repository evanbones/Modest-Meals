package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.regen.HealthRegenHelper;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Unique
    private Player mm$player = null;

    @Inject(
            method = "tick",
            at = @At("HEAD"),
            cancellable = true
    )
    public void mm$disableHunger(Player player, CallbackInfo callback) {
        if (mm$player == null) {
            mm$player = player;
        }
        if (ModConfig.get().disableHunger) {
            callback.cancel();
        }
    }

    @WrapMethod(method = "add")
    private void mm$consumeFoodProperties(int foodLevel, float saturationLevel, Operation<Void> original) {
        if (mm$player instanceof ServerPlayer && ModConfig.get().disableHunger) {
            HealthRegenHelper.get(mm$player).eat(foodLevel, saturationLevel, 0);
            return;
        }
        original.call(foodLevel, saturationLevel);
    }
}
