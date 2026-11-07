package com.evandev.modest_meals.network;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.config.ModConfig;
import com.google.gson.JsonParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public record ClientboundConfigSyncPayload(String json) implements CustomPacketPayload {
    public static final Type<ClientboundConfigSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "config_sync"));

    public static final StreamCodec<FriendlyByteBuf, ClientboundConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(Short.MAX_VALUE), ClientboundConfigSyncPayload::json,
            ClientboundConfigSyncPayload::new
    );

    public static ClientboundConfigSyncPayload create() {
        return new ClientboundConfigSyncPayload(ModConfig.serverSyncedValues().toString());
    }

    public static void sendTo(ServerPlayer player) {
        if (player.server.isSingleplayerOwner(player.getGameProfile())) {
            return;
        }
        PacketDistributor.sendToPlayer(player, create());
    }

    public static void broadcast() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendTo(player);
        }
    }

    public static void handle(ClientboundConfigSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ModConfig.applyServerValues(JsonParser.parseString(payload.json()).getAsJsonObject()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
