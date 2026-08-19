package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightCompat;
import com.evandev.modest_meals.compat.farmers_delight.NourishmentEffectHandler;
import com.evandev.modest_meals.config.HungerEffectOption;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.effect.ModMobEffects;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @WrapMethod(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"
    )
    boolean mm$addStatusEffect(
            MobEffectInstance effect, Entity source, Operation<Boolean> original
    ) {
        if (!(((LivingEntity) (Object) this) instanceof Player)) {
            return original.call(effect, source);
        }
        if (ModConfig.get().disableHunger && effect.is(MobEffects.HUNGER)) {
            HungerEffectOption hungerEffect = ModConfig.get().hungerEffect;
            if (hungerEffect == HungerEffectOption.DISABLED) {
                return false;
            }
            effect = new MobEffectInstance(
                    hungerEffect == HungerEffectOption.REPLACED_WITH_OTHER
                            ? ModConfig.getHungerReplacementEffect()
                            : ModMobEffects.STAMINA_DEPLETION,
                    hungerEffect == HungerEffectOption.REPLACED_WITH_OTHER
                            ? (int) (effect.getDuration() * ModConfig.get().hungerReplacementDurationMultiplier)
                            : effect.getDuration(),
                    effect.getAmplifier()
            );
        } else if (ModConfig.get().disableHunger && effect.is(MobEffects.SATURATION)) {
            effect = new MobEffectInstance(
                    ModMobEffects.STAMINA_REGEN, effect.getDuration(), effect.getAmplifier()
            );
        } else if (FarmersDelightCompat.isLoaded()) {
            effect = NourishmentEffectHandler.getEffectToApply(effect);
        }
        return original.call(effect, source);
    }

    @WrapOperation(
            method = "startUsingItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"
            )
    )
    private int mm$setCurrentHandMaxUseTime(
            ItemStack stack, LivingEntity user, Operation<Integer> original
    ) {
        if (ModConfig.get().instantEating && stack.get(DataComponents.FOOD) != null) {
            return 1;
        }
        return original.call(stack, user);
    }
}
