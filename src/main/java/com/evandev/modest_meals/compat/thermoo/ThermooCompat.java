package com.evandev.modest_meals.compat.thermoo;

import com.evandev.modest_meals.platform.Platform;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.Optional;

public final class ThermooCompat {
    public static final String MOD_ID = "thermoo";

    public static boolean isLoaded() {
        return Platform.isModLoaded(MOD_ID);
    }

    //? if thermoo {
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
    //?} else {
    /*public static Optional<Holder<Attribute>> frostResistance() {
        return Optional.empty();
    }

    public static Optional<Holder<Attribute>> heatResistance() {
        return Optional.empty();
    }

    public static Optional<Holder<Attribute>> environmentFrostResistance() {
        return Optional.empty();
    }

    public static Optional<Holder<Attribute>> environmentHeatResistance() {
        return Optional.empty();
    }
    *///?}
}
