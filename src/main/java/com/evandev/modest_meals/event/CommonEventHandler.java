package com.evandev.modest_meals.event;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.network.ClientboundStaminaSyncPayload;
import com.evandev.modest_meals.network.ModNetworking;
import com.evandev.modest_meals.stamina.PlayerStamina;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.evandev.modest_meals.stamina.StaminaHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class CommonEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        if (ModConfig.get().staminaSprint) {
            PlayerStamina stamina = StaminaHelper.get(player);
            stamina.tick();

            if (stamina.getData().isExhausted()) {
                if (player.isSprinting()) {
                    player.setSprinting(false);
                }
                if (player.isSwimming()) {
                    player.setSwimming(false);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        StaminaHelper.remove(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();

        if (original instanceof StaminaHolder origHolder && player instanceof StaminaHolder newHolder) {
            if (!event.isWasDeath()) {
                newHolder.mm$setStaminaData(origHolder.mm$getStaminaData());
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerStamina stamina = StaminaHelper.get(serverPlayer);
            stamina.reset();
            ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
        }
    }
}
