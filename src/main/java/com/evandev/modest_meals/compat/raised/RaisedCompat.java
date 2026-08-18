package com.evandev.modest_meals.compat.raised;

import net.neoforged.fml.ModList;

public class RaisedCompat {
    public static final String MOD_ID = "raised";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }
}
