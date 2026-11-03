package com.evandev.modest_meals.mixin.compat;

import com.evandev.modest_meals.compat.combat_roll.CombatRollCompat;
import net.combat_roll.internals.RollManager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = RollManager.class, remap = false)
public abstract class RollManagerMixin {

    @Inject(method = "isRollAvailable", at = @At("HEAD"), cancellable = true)
    private void mm$noRollWhenExhausted(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!CombatRollCompat.canRoll(player)) {
            cir.setReturnValue(false);
        }
    }
}
