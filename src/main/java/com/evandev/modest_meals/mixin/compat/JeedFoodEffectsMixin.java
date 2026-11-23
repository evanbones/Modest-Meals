package com.evandev.modest_meals.mixin.compat;

//? if neoforge_legacy {
import com.evandev.modest_meals.food.HungerEffectConversion;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.mehvahdjukaar.jeed.common.EffectInfo", remap = false)
public abstract class JeedFoodEffectsMixin {

    @WrapOperation(
            method = "buildStaticCache",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/food/FoodProperties$PossibleEffect;effect()Lnet/minecraft/world/effect/MobEffectInstance;"
            ),
            require = 0
    )
    private static MobEffectInstance mm$convertHunger(FoodProperties.PossibleEffect possibleEffect, Operation<MobEffectInstance> original) {
        MobEffectInstance effect = original.call(possibleEffect);
        return effect == null ? null : HungerEffectConversion.convert(effect);
    }
}
//?}
