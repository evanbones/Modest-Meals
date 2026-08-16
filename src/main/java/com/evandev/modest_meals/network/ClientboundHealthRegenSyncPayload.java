package com.evandev.modest_meals.network;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.regen.HealthRegenHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundHealthRegenSyncPayload(int consumedNutrition) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientboundHealthRegenSyncPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "health_regen_sync"));

    public static final StreamCodec<ByteBuf, ClientboundHealthRegenSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ClientboundHealthRegenSyncPayload::consumedNutrition,
                    ClientboundHealthRegenSyncPayload::new
            );

    public static void handle(ClientboundHealthRegenSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                HealthRegenHelper.get(player).setClientConsumedNutrition(payload.consumedNutrition());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
