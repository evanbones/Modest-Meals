package com.evandev.modest_meals.stamina;

import com.evandev.modest_meals.network.ClientboundStaminaSyncPayload;
import com.evandev.modest_meals.network.ModNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public abstract class StaminaHelper {
    private static final HashMap<UUID, PlayerStamina> PLAYER_DATA = new HashMap<>();

    public static void reset() {
        PLAYER_DATA.values().forEach(PlayerStamina::reset);

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ModNetworking.sendToPlayer(player, ClientboundStaminaSyncPayload.create(player));
            }
        }
    }

    public static PlayerStamina get(Player player) {
        return PLAYER_DATA.computeIfAbsent(player.getUUID(), uuid -> PlayerStamina.create(player));
    }

    public static Optional<PlayerStamina> find(UUID uuid) {
        return Optional.ofNullable(PLAYER_DATA.get(uuid));
    }

    public static void remove(Player player) {
        PLAYER_DATA.remove(player.getUUID());
    }

    public static void clear() {
        PLAYER_DATA.clear();
    }
}
