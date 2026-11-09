package com.evandev.modest_meals.compat.cold_sweat;

//? if neoforge_legacy {
/*import com.evandev.modest_meals.platform.Platform;
import net.neoforged.neoforge.common.NeoForge;

public final class ColdSweatCompat {
    public static final String MOD_ID = "cold_sweat";

    public static boolean isLoaded() {
        return Platform.isModLoaded(MOD_ID);
    }

    public static void register() {
        if (isLoaded()) {
            NeoForge.EVENT_BUS.register(ColdSweatThermalHandler.class);
        }
    }
}
*///?}
