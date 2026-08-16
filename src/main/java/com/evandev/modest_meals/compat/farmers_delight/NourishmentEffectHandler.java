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
        int nourishmentHealthBoostHeartsCount = ModConfig.get().nourishmentHealthBoostHeartsCount;
        int amplifier;
        if (nourishmentHealthBoostHeartsCount > 0) {
            effectType = ModMobEffects.NOURISHMENT_HEALTH_BOOST;
            amplifier = nourishmentHealthBoostHeartsCount - 1;
        } else {
            amplifier = effect.getAmplifier();
        }
        return new MobEffectInstance(
                effectType, effect.getDuration(), amplifier, effect.isAmbient(), false, true
        );
    }

    public static boolean isNourishmentHealthBoost(Holder<MobEffect> effect) {
        return effect == ModMobEffects.NOURISHMENT_HEALTH_BOOST;
    }

    public static Holder<MobEffect> getEffectForSprite(Holder<MobEffect> effect) {
        if (FarmersDelightCompat.isLoaded() && isNourishmentHealthBoost(effect)) {
            return ModEffects.NOURISHMENT;
        }
        return effect;
    }

    public static boolean playerHasEffect(Player player) {
        if (player.hasEffect(ModMobEffects.NOURISHMENT_HEALTH_BOOST)) {
            return true;
        }
        if (FarmersDelightCompat.isLoaded()) {
            return player.hasEffect(ModEffects.NOURISHMENT);
        }
        return false;
    }
}
