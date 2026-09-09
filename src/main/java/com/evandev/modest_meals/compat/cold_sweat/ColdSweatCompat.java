package com.evandev.modest_meals.compat.cold_sweat;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

public final class ColdSweatCompat {
    public static final String MOD_ID = "cold_sweat";

    private ColdSweatCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void register() {
        if (isLoaded()) {
            NeoForge.EVENT_BUS.register(ColdSweatThermalHandler.class);
        }
    }
}
