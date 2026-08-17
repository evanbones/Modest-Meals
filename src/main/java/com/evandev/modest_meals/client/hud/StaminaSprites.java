package com.evandev.modest_meals.client.hud;

import com.evandev.modest_meals.Constants;
import net.minecraft.resources.ResourceLocation;

public class StaminaSprites {

    public static final ResourceLocation STAMINA_LEVEL_OTHER_HALF = ResourceLocation.fromNamespaceAndPath(
            Constants.MOD_ID, "stamina_level_other_half"
    );

    private static volatile boolean otherHalfAvailable = false;

    public static boolean isOtherHalfAvailable() {
        return otherHalfAvailable;
    }

    static void setOtherHalfAvailable(boolean available) {
        otherHalfAvailable = available;
    }
}
