package com.evandev.modest_meals.stamina;

import com.evandev.modest_meals.network.ClientboundStaminaSyncPayload;
import com.evandev.modest_meals.network.ModNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class StaminaHelper {
    private static final Map<UUID, PlayerStamina> SERVER_DATA = new ConcurrentHashMap<>();
    private static final Map<UUID, PlayerStamina> CLIENT_DATA = new ConcurrentHashMap<>();

    public static void reset() {
        SERVER_DATA.values().forEach(PlayerStamina::reset);
        CLIENT_DATA.values().forEach(PlayerStamina::reset);

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ModNetworking.sendToPlayer(player, ClientboundStaminaSyncPayload.create(player));
            }
        }
    }

    public static PlayerStamina get(Player player) {
        Map<UUID, PlayerStamina> data = player.level().isClientSide() ? CLIENT_DATA : SERVER_DATA;
        return data.compute(player.getUUID(), (uuid, existing) ->
                existing != null && existing.getPlayer() == player ? existing : PlayerStamina.create(player)
        );
    }

    public static Optional<PlayerStamina> find(UUID uuid) {
        PlayerStamina server = SERVER_DATA.get(uuid);
        if (server != null) {
            return Optional.of(server);
        }
        return Optional.ofNullable(CLIENT_DATA.get(uuid));
    }

    public static void remove(Player player) {
        if (player.level().isClientSide()) {
            CLIENT_DATA.remove(player.getUUID());
        } else {
            SERVER_DATA.remove(player.getUUID());
        }
    }

    public static void clear() {
        SERVER_DATA.clear();
        CLIENT_DATA.clear();
    }
}
