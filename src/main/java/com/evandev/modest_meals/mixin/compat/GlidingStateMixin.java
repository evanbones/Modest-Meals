package com.evandev.modest_meals.mixin.compat;

import com.evandev.modest_meals.api.StaminaApi;
import com.evandev.modest_meals.compat.reliable_gliders.ReliableGlidersCompat;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.evandev.reliable_gliders.api.GlidingState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = GlidingState.class, remap = false)
public abstract class GlidingStateMixin {

    @Inject(method = "isGliding", at = @At("HEAD"), cancellable = true)
    private static void mm$stopGlidingWhenExhausted(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.get().staminaGliding || !StaminaApi.isEnabled() || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.level().isClientSide() && !player.isLocalPlayer()) {
            return;
        }
        if (StaminaHelper.get(player).getData().isExhausted()) {
            if (GlidingState.wasGliding(player) && player instanceof ServerPlayer serverPlayer) {
                ReliableGlidersCompat.startExhaustedFall(serverPlayer);
            }
            GlidingState.setGliding(player, false);
            cir.setReturnValue(false);
        }
    }
}
