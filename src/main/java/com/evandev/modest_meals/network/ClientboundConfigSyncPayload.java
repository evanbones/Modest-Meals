package com.evandev.modest_meals.network;

import com.evandev.modest_meals.platform.Platform;
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
        MinecraftServer server = Platform.getCurrentServer();
        //? if >=26.1 {
        /*if (server == null || server.isSingleplayerOwner(player.nameAndId())) {
        *///?} else
        if (server == null || server.isSingleplayerOwner(player.getGameProfile())) {
            return;
        }
        Platform.sendToPlayer(player, create());
    }

    public static void broadcast() {
        MinecraftServer server = Platform.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendTo(player);
        }
    }

    public static void handle(ClientboundConfigSyncPayload payload, ClientPacketContext context) {
        ModConfig.applyServerValues(JsonParser.parseString(payload.json()).getAsJsonObject());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
