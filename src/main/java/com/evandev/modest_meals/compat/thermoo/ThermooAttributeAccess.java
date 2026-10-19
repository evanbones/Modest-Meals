package com.evandev.modest_meals.compat.thermoo;

import com.github.thedeathlycow.thermoo.api.ThermooAttributes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

final class ThermooAttributeAccess {

    private ThermooAttributeAccess() {
    }

    static Holder<Attribute> frostResistance() {
        return ThermooAttributes.FROST_RESISTANCE;
    }

    static Holder<Attribute> heatResistance() {
        return ThermooAttributes.HEAT_RESISTANCE;
    }

    static Holder<Attribute> environmentFrostResistance() {
        return ThermooAttributes.ENVIRONMENT_FROST_RESISTANCE;
    }

    static Holder<Attribute> environmentHeatResistance() {
        return ThermooAttributes.ENVIRONMENT_HEAT_RESISTANCE;
    }
}
