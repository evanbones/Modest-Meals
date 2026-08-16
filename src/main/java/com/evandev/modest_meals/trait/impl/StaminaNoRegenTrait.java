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

public record StaminaNoRegenTrait(int duration) implements FoodTrait {
    public static final MapCodec<StaminaNoRegenTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("duration").forGetter(StaminaNoRegenTrait::duration)
    ).apply(instance, StaminaNoRegenTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StaminaNoRegenTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StaminaNoRegenTrait::duration,
            StaminaNoRegenTrait::new
    );

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.STAMINA_NO_REGEN;
    }

    @Override
    public FoodTrait compoundWith(FoodTrait other, float valueMultiplier, float durationMultiplier) {
        if (other instanceof StaminaNoRegenTrait(int duration1)) {
            return new StaminaNoRegenTrait((int) ((this.duration + duration1) * durationMultiplier));
        }
        return this;
    }

    @Override
    public void apply(LivingEntity entity, float valueMultiplier, float durationMultiplier) {
        int dur = (int) (this.duration * durationMultiplier);
        if (dur > 0) {
            entity.addEffect(new MobEffectInstance(ModMobEffects.STAMINA_NO_REGEN, dur, 0));
        }
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        return TraitTooltipHelper.formatSimpleTake("modest_meals.trait.stamina_no_regen", (long) (this.duration * durationMultiplier));
    }
}
