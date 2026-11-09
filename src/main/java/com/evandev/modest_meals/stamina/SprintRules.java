package com.evandev.modest_meals.stamina;

import com.evandev.modest_meals.config.ModConfig;
import net.minecraft.world.entity.player.Player;

import java.util.function.BooleanSupplier;

public final class SprintRules {
    public static boolean hasEnoughFoodToSprint(Player player, BooleanSupplier vanilla) {
        if (player.isPassenger() || player.getAbilities().mayfly) {
            return true;
        }
        if (!ModConfig.get().staminaSprint || !ModConfig.get().staminaSprinting) {
            switch (ModConfig.get().sprinting) {
                case DISABLED -> {
                    if (!player.isUnderWater()) {
                        return false;
                    }
                }
                case LIMITED_BY_HEALTH -> {
                    if (player.getHealth() <= ModConfig.get().sprintingHealthLimit) {
                        return false;
                    }
                }
                case VANILLA -> {
                }
            }
        }
        return ModConfig.get().disableHunger || vanilla.getAsBoolean();
    }
}
