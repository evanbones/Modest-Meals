package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
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

    @WrapMethod(method = "hasEnoughFoodToStartSprinting")
    private boolean mm$canPlayerSprint(Operation<Boolean> original) {
        if (this.isPassenger() || this.getAbilities().mayfly) {
            return true;
        }
        if (!ModConfig.get().staminaSprint) {
            switch (ModConfig.get().sprinting) {
                case DISABLED -> {
                    if (!this.isUnderWater()) {
                        return false;
                    }
                }
                case LIMITED_BY_HEALTH -> {
                    if (this.getHealth() <= ModConfig.get().sprintingHealthLimit) {
                        return false;
                    }
                }
                case VANILLA -> {
                }
            }
        }
        if (ModConfig.get().disableHunger) {
            return true;
        }
        return original.call();
    }
}
