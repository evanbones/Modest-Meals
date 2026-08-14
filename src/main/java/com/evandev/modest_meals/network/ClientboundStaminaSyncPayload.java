package com.evandev.modest_meals.network;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.stamina.StaminaData;
import com.evandev.modest_meals.stamina.StaminaHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundStaminaSyncPayload(int stamina, int remaining, int cooldown,
                                            boolean exhausted) implements CustomPacketPayload {
    public static final Type<ClientboundStaminaSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "stamina_sync"));

    public static final StreamCodec<FriendlyByteBuf, ClientboundStaminaSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClientboundStaminaSyncPayload::stamina,
            ByteBufCodecs.VAR_INT, ClientboundStaminaSyncPayload::remaining,
            ByteBufCodecs.VAR_INT, ClientboundStaminaSyncPayload::cooldown,
            ByteBufCodecs.BOOL, ClientboundStaminaSyncPayload::exhausted,
            ClientboundStaminaSyncPayload::new
    );

    public static ClientboundStaminaSyncPayload create(ServerPlayer player) {
        StaminaData data = StaminaHelper.get(player).getData();
        return new ClientboundStaminaSyncPayload(data.getStamina(), data.getRemaining(), data.getCooldown(), data.isExhausted());
    }

    public static void handle(ClientboundStaminaSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            StaminaData data = StaminaHelper.get(player).getData();
            data.setStaminaRaw(payload.stamina());
            data.setRemaining(payload.remaining());
            data.setCooldown(payload.cooldown());
            data.setExhausted(payload.exhausted());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
