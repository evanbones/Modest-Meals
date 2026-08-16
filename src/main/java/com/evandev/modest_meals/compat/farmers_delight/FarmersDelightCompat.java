package com.evandev.modest_meals.compat.farmers_delight;

import net.neoforged.fml.ModList;

public class FarmersDelightCompat {
    public static final String MOD_ID = "farmersdelight";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }
}
