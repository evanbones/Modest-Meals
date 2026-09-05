package com.evandev.modest_meals.trait.impl;

import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitType;
import com.evandev.modest_meals.trait.TraitBenefit;
import com.evandev.modest_meals.trait.TraitTooltipHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record FireExtinguishTrait() implements FoodTrait {
    public static final FireExtinguishTrait INSTANCE = new FireExtinguishTrait();
    public static final MapCodec<FireExtinguishTrait> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, FireExtinguishTrait> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.FIRE_EXTINGUISH;
    }

    @Override
    public TraitBenefit benefit() {
        return TraitBenefit.UTILITY;
    }

    @Override
    public FoodTrait compoundWith(FoodTrait other, float valueMultiplier, float durationMultiplier) {
        return this;
    }

    @Override
    public void apply(LivingEntity entity, ItemStack stack, float valueMultiplier, float durationMultiplier) {
        entity.clearFire();
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        return TraitTooltipHelper.formatSimplePlus("modest_meals.trait.fire_extinguish", 0);
    }
}
