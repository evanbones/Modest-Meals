package com.evandev.modest_meals.trait.impl;

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
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record TemporaryHealthTrait(float value) implements FoodTrait {
    private static final int DURATION_TICKS = 2400;
    private static final float ABSORPTION_PER_LEVEL = 4.0F;

    public static final MapCodec<TemporaryHealthTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("value").forGetter(TemporaryHealthTrait::value)
    ).apply(instance, TemporaryHealthTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemporaryHealthTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, TemporaryHealthTrait::value,
            TemporaryHealthTrait::new
    );

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.TEMPORARY_HEALTH;
    }

    @Override
    public TraitBenefit benefit() {
        return TraitBenefit.UTILITY;
    }

    @Override
    public void apply(LivingEntity entity, ItemStack stack, float valueMultiplier, float durationMultiplier) {
        float points = this.value * valueMultiplier;
        if (points <= 0.0F || entity.level().isClientSide()) {
            return;
        }
        float target = Math.max(entity.getAbsorptionAmount(), points);
        int amplifier = Math.max(0, Mth.ceil(target / ABSORPTION_PER_LEVEL) - 1);
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, DURATION_TICKS, amplifier));
        entity.setAbsorptionAmount(target);
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier, float tickRate) {
        return TraitTooltipHelper.formatPlusTrait("modest_meals.trait.temporary_health",
                this.value * valueMultiplier, 0, tickRate);
    }
}
