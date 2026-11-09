package com.evandev.modest_meals.mixin;

//? if >=26.1 {
/*import com.evandev.modest_meals.food.BuiltInFoodEffects;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ApplyStatusEffectsConsumeEffect.class)
public class ApplyStatusEffectsConsumeEffectMixin {
    @WrapOperation(method = "apply", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private boolean mm$applyKeptFoodEffect(LivingEntity entity, MobEffectInstance effect, Operation<Boolean> original,
                                           @Local(argsOnly = true) ItemStack stack) {
        return BuiltInFoodEffects.isKept(stack, effect) && original.call(entity, effect);
    }
}
*///?}
