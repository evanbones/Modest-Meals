package com.evandev.modest_meals.regen;

import net.minecraft.world.entity.player.Player;

public class HealthRegenHelper {

    public static PlayerHealthRegen get(Player player) {
        if (player instanceof HealthRegenHolder holder) {
            return holder.mm$getHealthRegen();
        }
        return new PlayerHealthRegen(player);
    }
}
