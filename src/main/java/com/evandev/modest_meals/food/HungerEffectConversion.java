package com.evandev.modest_meals.food;

import com.evandev.modest_meals.config.HungerEffectOption;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.effect.ModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.Nullable;

public class HungerEffectConversion {
    public static boolean applies(MobEffectInstance effect) {
        return ModConfig.get().disableHunger && effect.is(MobEffects.HUNGER);
    }

    @Nullable
    public static MobEffectInstance convert(MobEffectInstance effect) {
        if (!applies(effect)) {
            return effect;
        }
        HungerEffectOption option = ModConfig.get().hungerEffect;
        if (option == HungerEffectOption.DISABLED) {
            return null;
        }
        if (option == HungerEffectOption.REPLACED_WITH_OTHER) {
            return new MobEffectInstance(ModConfig.getHungerReplacementEffect(),
                    (int) (effect.getDuration() * ModConfig.get().hungerReplacementDurationMultiplier),
                    effect.getAmplifier());
        }
        return new MobEffectInstance(ModMobEffects.STAMINA_DEPLETION, effect.getDuration(), effect.getAmplifier());
    }
}
