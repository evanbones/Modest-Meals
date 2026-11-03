package com.evandev.modest_meals.compat.reliable_gliders;

import com.evandev.reliable_gliders.api.GlidingState;
import net.minecraft.world.entity.player.Player;

final class ReliableGlidersBridge {

    static boolean wasGliding(Player player) {
        return GlidingState.wasGliding(player);
    }
}
