package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    private LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @ModifyReturnValue(method = "canStartSprinting", at = @At("RETURN"))
    private boolean mm$modifyCanStartSprinting(boolean canSprint) {
        if (!ModConfig.get().staminaSprint || this.isCreative() || this.isSpectator()) {
            return canSprint;
        }

        if (StaminaHelper.get(this).getData().isExhausted()) {
            return false;
        }

        return canSprint;
    }
}
