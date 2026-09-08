package com.evandev.modest_meals.compat.thermoo;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.fml.ModList;

import java.util.Optional;

public final class ThermooCompat {
    public static final String MOD_ID = "thermoo";

    private ThermooCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static Optional<Holder<Attribute>> frostResistance() {
        return isLoaded() ? Optional.of(ThermooAttributeAccess.frostResistance()) : Optional.empty();
    }

    public static Optional<Holder<Attribute>> heatResistance() {
        return isLoaded() ? Optional.of(ThermooAttributeAccess.heatResistance()) : Optional.empty();
    }

    public static Optional<Holder<Attribute>> environmentFrostResistance() {
        return isLoaded() ? Optional.of(ThermooAttributeAccess.environmentFrostResistance()) : Optional.empty();
    }

    public static Optional<Holder<Attribute>> environmentHeatResistance() {
        return isLoaded() ? Optional.of(ThermooAttributeAccess.environmentHeatResistance()) : Optional.empty();
    }
}
