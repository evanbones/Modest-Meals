package com.evandev.modest_meals.network;

import net.minecraft.world.entity.player.Player;

public record ClientPacketContext(Player player, boolean isMemoryConnection) {
}
