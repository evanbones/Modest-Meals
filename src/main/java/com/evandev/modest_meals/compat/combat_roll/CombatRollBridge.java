package com.evandev.modest_meals.compat.combat_roll;

import net.combat_roll.api.event.ServerSideRollEvents;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

final class CombatRollBridge {

    static void onRoll(Consumer<ServerPlayer> handler) {
        ServerSideRollEvents.PLAYER_START_ROLLING.register((player, velocity) -> handler.accept(player));
    }
}
