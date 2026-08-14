package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.config.ModConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @Unique
    private int mm$restoreHealthAmount = 0;

    @Shadow
    private int tickTimer;

    @Shadow
    private float exhaustionLevel;

    @Inject(method = "eat(IF)V", at = @At("HEAD"))
    private void mm$onEat(int foodLevelModifier, float saturationLevelModifier, CallbackInfo ci) {
        if (ModConfig.get().disableHunger) {
            this.mm$restoreHealthAmount = foodLevelModifier;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void mm$onTick(Player player, CallbackInfo ci) {
        if (this.mm$restoreHealthAmount > 0) {
            player.heal(this.mm$restoreHealthAmount);
            this.mm$restoreHealthAmount = 0;
        }

        if (!ModConfig.get().disableHunger) {
            return;
        }

        this.exhaustionLevel = 0.0F;
        this.tickTimer = 0;
    }
}
