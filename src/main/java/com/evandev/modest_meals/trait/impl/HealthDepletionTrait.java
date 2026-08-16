package com.evandev.modest_meals.trait.impl;

import com.evandev.modest_meals.effect.ModMobEffects;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitType;
import com.evandev.modest_meals.trait.TraitTooltipHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public record HealthDepletionTrait(float value, int duration) implements FoodTrait {
    public static final MapCodec<HealthDepletionTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("value").forGetter(HealthDepletionTrait::value),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(HealthDepletionTrait::duration)
    ).apply(instance, HealthDepletionTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HealthDepletionTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, HealthDepletionTrait::value,
            ByteBufCodecs.VAR_INT, HealthDepletionTrait::duration,
            HealthDepletionTrait::new
    );

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.HEALTH_DEPLETION;
    }

    @Override
    public FoodTrait compoundWith(FoodTrait other, float valueMultiplier, float durationMultiplier) {
        if (other instanceof HealthDepletionTrait(float value1, int duration1)) {
            return new HealthDepletionTrait(
                    (this.value + value1) * valueMultiplier,
                    (int) ((this.duration + duration1) * durationMultiplier)
            );
        }
        return this;
    }

    @Override
    public void apply(LivingEntity entity, float valueMultiplier, float durationMultiplier) {
        float finalVal = this.value * valueMultiplier;
        int dur = (int) (this.duration * durationMultiplier);
        if (dur > 0) {
            int amp = Math.max(0, Math.round(finalVal) - 1);
            entity.addEffect(new MobEffectInstance(ModMobEffects.HEALTH_DEPLETION, dur, amp));
        } else {
            entity.hurt(entity.damageSources().magic(), finalVal);
        }
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        return TraitTooltipHelper.formatTakeTrait("modest_meals.trait.health_depletion",
                this.value * valueMultiplier, (long) (this.duration * durationMultiplier));
    }
}
