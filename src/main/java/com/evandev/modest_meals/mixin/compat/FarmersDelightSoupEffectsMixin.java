package com.evandev.modest_meals.mixin.compat;

import com.evandev.modest_meals.food.BuiltInFoodEffects;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "vectorwing.farmersdelight.common.event.CommonEvents", remap = false)
public abstract class FarmersDelightSoupEffectsMixin {

    @WrapOperation(
            method = "handleVanillaSoupEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"
            )
    )
    private static boolean mm$applyKeptSoupEffect(LivingEntity entity, MobEffectInstance effect, Operation<Boolean> original,
                                                  @Local(argsOnly = true) LivingEntityUseItemEvent.Finish event) {
        if (!BuiltInFoodEffects.isKept(event.getItem(), effect)) {
            return false;
        }
        return original.call(entity, effect);
    }
}
