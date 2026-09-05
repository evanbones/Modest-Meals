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

public record StaminaCapacityTrait(float value, int duration) implements FoodTrait {

    public static final MapCodec<StaminaCapacityTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("value", 1.0f).forGetter(StaminaCapacityTrait::value),
            Codec.INT.fieldOf("duration").forGetter(StaminaCapacityTrait::duration)
    ).apply(instance, StaminaCapacityTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StaminaCapacityTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, StaminaCapacityTrait::value,
            ByteBufCodecs.VAR_INT, StaminaCapacityTrait::duration,
            StaminaCapacityTrait::new
    );

    private static final float CAPACITY_PER_LEVEL = 4.0f;

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.STAMINA_CAPACITY;
    }

    @Override
    public TraitBenefit benefit() {
        return TraitBenefit.UTILITY;
    }

    @Override
    public FoodTrait compoundWith(FoodTrait other, float valueMultiplier, float durationMultiplier) {
        if (other instanceof StaminaCapacityTrait(float value1, int duration1)) {
            return new StaminaCapacityTrait(
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
            entity.addEffect(new MobEffectInstance(ModMobEffects.STAMINA_BOOST, dur, amp));
        }
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        double levels = Math.max(1, Math.round(this.value * valueMultiplier));
        return TraitTooltipHelper.formatPlusTrait("modest_meals.trait.stamina_capacity",
                levels * CAPACITY_PER_LEVEL, (long) (this.duration * durationMultiplier));
    }
}
