package com.evandev.modest_meals.trait.impl;

import com.evandev.modest_meals.api.StaminaApi;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record StaminaAdditionTrait(float value) implements FoodTrait {
    public static final MapCodec<StaminaAdditionTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("value").forGetter(StaminaAdditionTrait::value)
    ).apply(instance, StaminaAdditionTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StaminaAdditionTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, StaminaAdditionTrait::value,
            StaminaAdditionTrait::new
    );

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.STAMINA_ADDITION;
    }

    @Override
    public TraitBenefit benefit() {
        return TraitBenefit.STAMINA;
    }

    @Override
    public void apply(LivingEntity entity, ItemStack stack, float valueMultiplier, float durationMultiplier) {
        if (entity instanceof Player player) {
            StaminaApi.restore(player, this.value * valueMultiplier);
        }
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier, float tickRate) {
        return TraitTooltipHelper.formatPlusTrait("modest_meals.trait.stamina_addition",
                this.value * valueMultiplier, 0, tickRate);
    }
}
