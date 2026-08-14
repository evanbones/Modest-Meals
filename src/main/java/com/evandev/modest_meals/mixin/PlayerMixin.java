package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.stamina.StaminaCodec;
import com.evandev.modest_meals.stamina.StaminaData;
import com.evandev.modest_meals.stamina.StaminaHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements StaminaHolder {
    @Unique
    private StaminaData mm$staminaData = StaminaData.create();

    @Override
    public @NotNull StaminaData mm$getStaminaData() {
        return this.mm$staminaData;
    }

    @Override
    public void mm$setStaminaData(@NotNull StaminaData data) {
        this.mm$staminaData = data;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void mm$onAddAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        StaminaCodec.save((Player) (Object) this, compound);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void mm$onReadAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        StaminaCodec.read((Player) (Object) this, compound);
    }
}
