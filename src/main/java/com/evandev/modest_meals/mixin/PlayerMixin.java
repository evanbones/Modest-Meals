package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.stamina.StaminaCodec;
import com.evandev.modest_meals.stamina.StaminaData;
import com.evandev.modest_meals.stamina.StaminaHolder;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(method = "eat", at = @At("HEAD"))
    private void mm$onPlayerEat(Level level, ItemStack itemStack, FoodProperties foodProperties, CallbackInfoReturnable<ItemStack> cir) {
        if (ModConfig.get().disableHunger && itemStack.has(DataComponents.FOOD)) {
            FoodProperties food = itemStack.get(DataComponents.FOOD);
            int healAmount = food != null ? food.nutrition() : 0;
            ((Player) (Object) this).heal(healAmount);
        }
    }

    @ModifyReturnValue(method = "canEat", at = @At("RETURN"))
    private boolean mm$canPlayerEat(boolean playerCanEat, boolean canAlwaysEat) {
        if (ModConfig.get().disableHunger && !canAlwaysEat) {
            Player player = (Player) (Object) this;
            return Mth.ceil(player.getHealth()) < Mth.ceil(player.getMaxHealth());
        }
        return playerCanEat;
    }

    @WrapWithCondition(
            method = "eat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"
            )
    )
    private boolean mm$shouldPlayerEat(FoodData foodData, FoodProperties foodProperties) {
        return !ModConfig.get().disableHunger;
    }
}
