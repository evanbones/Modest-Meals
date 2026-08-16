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

public record StaminaDepletionTrait(float value, int duration) implements FoodTrait {
    public static final MapCodec<StaminaDepletionTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("value", 1.0f).forGetter(StaminaDepletionTrait::value),
            Codec.INT.fieldOf("duration").forGetter(StaminaDepletionTrait::duration)
    ).apply(instance, StaminaDepletionTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StaminaDepletionTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, StaminaDepletionTrait::value,
            ByteBufCodecs.VAR_INT, StaminaDepletionTrait::duration,
            StaminaDepletionTrait::new
    );

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.STAMINA_DEPLETION;
    }

    @Override
    public FoodTrait compoundWith(FoodTrait other, float valueMultiplier, float durationMultiplier) {
        if (other instanceof StaminaDepletionTrait(float value1, int duration1)) {
            return new StaminaDepletionTrait(
                    (this.value + value1) * valueMultiplier,
                    (int) ((this.duration + duration1) * durationMultiplier)
            );
        }
        return this;
    }

    @Override
    public void apply(LivingEntity entity, float valueMultiplier, float durationMultiplier) {
        int amp = Math.max(0, Math.round(this.value * valueMultiplier) - 1);
        int dur = (int) (this.duration * durationMultiplier);
        if (dur > 0) {
            entity.addEffect(new MobEffectInstance(ModMobEffects.STAMINA_DEPLETION, dur, amp));
        }
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        return TraitTooltipHelper.formatTakeTrait("modest_meals.trait.stamina_depletion",
                this.value * valueMultiplier, (long) (this.duration * durationMultiplier));
    }
}
