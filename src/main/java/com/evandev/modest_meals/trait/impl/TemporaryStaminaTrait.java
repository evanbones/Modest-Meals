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

public record TemporaryStaminaTrait(float value) implements FoodTrait {
    public static final MapCodec<TemporaryStaminaTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("value").forGetter(TemporaryStaminaTrait::value)
    ).apply(instance, TemporaryStaminaTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TemporaryStaminaTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, TemporaryStaminaTrait::value,
            TemporaryStaminaTrait::new
    );

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.TEMPORARY_STAMINA;
    }

    @Override
    public TraitBenefit benefit() {
        return TraitBenefit.UTILITY;
    }

    @Override
    public void apply(LivingEntity entity, ItemStack stack, float valueMultiplier, float durationMultiplier) {
        if (!(entity instanceof Player player)) {
            return;
        }
        StaminaApi.grantTemporary(player, this.value * valueMultiplier);
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        return TraitTooltipHelper.formatPlusTrait("modest_meals.trait.temporary_stamina",
                this.value * valueMultiplier, 0);
    }
}
