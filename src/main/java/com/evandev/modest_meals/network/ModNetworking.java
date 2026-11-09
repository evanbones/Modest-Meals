package com.evandev.modest_meals.network;

import com.evandev.modest_meals.platform.Platform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public class ModNetworking {
    public static final String PROTOCOL_VERSION = "2.0.0";

    public static final List<ClientboundPayload<?>> CLIENTBOUND = List.of(
            new ClientboundPayload<>(ClientboundConfigSyncPayload.TYPE, ClientboundConfigSyncPayload.STREAM_CODEC, ClientboundConfigSyncPayload::handle),
            new ClientboundPayload<>(ClientboundStaminaSyncPayload.TYPE, ClientboundStaminaSyncPayload.STREAM_CODEC, ClientboundStaminaSyncPayload::handle),
            new ClientboundPayload<>(ClientboundHealthRegenSyncPayload.TYPE, ClientboundHealthRegenSyncPayload.STREAM_CODEC, ClientboundHealthRegenSyncPayload::handle),
            new ClientboundPayload<>(ClientboundFoodDataSyncPayload.TYPE, ClientboundFoodDataSyncPayload.STREAM_CODEC, ClientboundFoodDataSyncPayload::handle)
    );

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        Platform.sendToPlayer(player, payload);
    }
}
