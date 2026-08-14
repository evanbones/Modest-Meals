package com.evandev.modest_meals.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1.0.0");
        registrar.playToClient(
                ClientboundStaminaSyncPayload.TYPE,
                ClientboundStaminaSyncPayload.STREAM_CODEC,
                ClientboundStaminaSyncPayload::handle
        );
    }

    public static void sendToPlayer(ServerPlayer player, ClientboundStaminaSyncPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
