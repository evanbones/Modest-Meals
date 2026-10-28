package com.evandev.modest_meals.trait;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface FoodTrait {
    FoodTraitType<?> getType();

    default TraitBenefit benefit() {
        return TraitBenefit.NONE;
    }

    void apply(LivingEntity entity, ItemStack stack, float valueMultiplier, float durationMultiplier);

    Component getTooltipComponent(double valueMultiplier, double durationMultiplier, float tickRate);
}
