package com.evandev.modest_meals.compat.reliable_gliders;

import com.evandev.modest_meals.platform.Platform;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.mixin.ServerGamePacketListenerImplAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class ReliableGlidersCompat {
    public static final String MOD_ID = "reliable_gliders";

    private static final Set<ServerPlayer> EXHAUSTED_FALLS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = Platform.isModLoaded(MOD_ID);
        }
        return loaded;
    }

    public static boolean isGliding(Player player) {
        return ModConfig.get().staminaGliding && isLoaded() && ReliableGlidersBridge.wasGliding(player);
    }

    public static void startExhaustedFall(ServerPlayer player) {
        EXHAUSTED_FALLS.add(player);
    }

    public static void tickExhaustedFall(ServerPlayer player) {
        if (!EXHAUSTED_FALLS.contains(player)) {
            return;
        }
        if (player.onGround() || player.isInWater() || player.onClimbable() || player.isPassenger()
                || player.isFallFlying() || player.getAbilities().flying || player.isDeadOrDying()
                || player.connection == null) {
            EXHAUSTED_FALLS.remove(player);
            return;
        }
        ((ServerGamePacketListenerImplAccessor) player.connection).mm$setAboveGroundTickCount(0);
    }
}
