package com.evandev.modest_meals.trait.impl;

import com.evandev.modest_meals.effect.ModMobEffects;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitType;
import com.evandev.modest_meals.trait.TraitBenefit;
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
import net.minecraft.world.item.ItemStack;

public record HealthRegenTrait(float value, int duration) implements FoodTrait {
    public static final MapCodec<HealthRegenTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("value", 1.0f).forGetter(HealthRegenTrait::value),
            Codec.INT.fieldOf("duration").forGetter(HealthRegenTrait::duration)
    ).apply(instance, HealthRegenTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HealthRegenTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, HealthRegenTrait::value,
            ByteBufCodecs.VAR_INT, HealthRegenTrait::duration,
            HealthRegenTrait::new
    );

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.HEALTH_REGEN;
    }

    @Override
    public TraitBenefit benefit() {
        return TraitBenefit.HEALTH;
    }

    @Override
    public FoodTrait compoundWith(FoodTrait other, float valueMultiplier, float durationMultiplier) {
        if (other instanceof HealthRegenTrait(float value1, int duration1)) {
            return new HealthRegenTrait(
                    (this.value + value1) * valueMultiplier,
                    (int) ((this.duration + duration1) * durationMultiplier)
            );
        }
        return this;
    }

    @Override
    public void apply(LivingEntity entity, ItemStack stack, float valueMultiplier, float durationMultiplier) {
        int amp = Math.max(0, Math.round(this.value * valueMultiplier) - 1);
        int dur = (int) (this.duration * durationMultiplier);
        if (dur > 0) {
            entity.addEffect(new MobEffectInstance(ModMobEffects.HEALTH_REGEN, dur, amp));
        }
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        return TraitTooltipHelper.formatPlusTrait("modest_meals.trait.health_regen",
                this.value * valueMultiplier, (long) (this.duration * durationMultiplier));
    }
}
