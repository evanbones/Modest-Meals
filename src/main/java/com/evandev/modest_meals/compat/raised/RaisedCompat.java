package com.evandev.modest_meals.compat.raised;

import com.evandev.modest_meals.platform.Platform;

public class RaisedCompat {
    public static final String MOD_ID = "raised";

    public static boolean isLoaded() {
        return Platform.isModLoaded(MOD_ID);
    }
}
