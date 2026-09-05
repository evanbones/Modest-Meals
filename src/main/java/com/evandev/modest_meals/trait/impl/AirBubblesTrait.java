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

public record AirBubblesTrait(int value) implements FoodTrait {
    public static final MapCodec<AirBubblesTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("value", 100).forGetter(AirBubblesTrait::value)
    ).apply(instance, AirBubblesTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AirBubblesTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AirBubblesTrait::value,
            AirBubblesTrait::new
    );

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.AIR_BUBBLES;
    }

    @Override
    public TraitBenefit benefit() {
        return TraitBenefit.UTILITY;
    }

    @Override
    public FoodTrait compoundWith(FoodTrait other, float valueMultiplier, float durationMultiplier) {
        if (other instanceof AirBubblesTrait(int value1)) {
            return new AirBubblesTrait((int) ((this.value + value1) * valueMultiplier));
        }
        return this;
    }

    @Override
    public void apply(LivingEntity entity, ItemStack stack, float valueMultiplier, float durationMultiplier) {
        int addAir = (int) (this.value * valueMultiplier);
        entity.setAirSupply(Math.min(entity.getMaxAirSupply(), entity.getAirSupply() + addAir));
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        int bubbles = (int) Math.round((this.value * valueMultiplier) / 30.0);
        return TraitTooltipHelper.formatPlusTrait("modest_meals.trait.air_bubbles", bubbles > 0 ? bubbles : 1, 0);
    }
}
