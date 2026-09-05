package com.evandev.modest_meals.trait;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface FoodTrait {
    FoodTraitType<?> getType();

    /**
     * What this trait offers the eater. Defaults to {@link TraitBenefit#NONE}.
     */
    default TraitBenefit benefit() {
        return TraitBenefit.NONE;
    }

    /**
     * Compounds this trait with another trait of the same type (from multiple ingredients).
     */
    FoodTrait compoundWith(FoodTrait other, float valueMultiplier, float durationMultiplier);

    /**
     * Applies this trait's effect when the food is consumed by an entity.
     */
    void apply(LivingEntity entity, ItemStack stack, float valueMultiplier, float durationMultiplier);

    /**
     * Produces a formatted tooltip line.
     */
    Component getTooltipComponent(double valueMultiplier, double durationMultiplier);
}
