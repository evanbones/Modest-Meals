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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record TemporaryHealthTrait(float value) implements FoodTrait {
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
        if (points > 0.0F) {
            entity.setAbsorptionAmount(entity.getAbsorptionAmount() + points);
        }
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        return TraitTooltipHelper.formatPlusTrait("modest_meals.trait.temporary_health",
                this.value * valueMultiplier, 0);
    }
}
