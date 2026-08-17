package com.evandev.modest_meals.compat.farmers_delight;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.effect.ModMobEffects;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import vectorwing.farmersdelight.common.registry.ModEffects;

public class NourishmentEffectHandler {

    public static MobEffectInstance getEffectToApply(MobEffectInstance effect) {
        if (!FarmersDelightCompat.isLoaded()) {
            return effect;
        }
        Holder<MobEffect> effectType = effect.getEffect();
        if (effectType != ModEffects.NOURISHMENT || !ModConfig.get().disableHunger) {
            return effect;
        }
        return new MobEffectInstance(
                ModMobEffects.STAMINA_NOURISHMENT, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), false, true
        );
    }
}
