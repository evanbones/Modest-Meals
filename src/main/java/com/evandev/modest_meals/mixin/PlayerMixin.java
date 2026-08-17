package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.food.FoodConsumption;
import com.evandev.modest_meals.food.FoodValues;
import com.evandev.modest_meals.regen.HealthRegenHolder;
import com.evandev.modest_meals.regen.PlayerHealthRegen;
import com.evandev.modest_meals.stamina.StaminaCodec;
import com.evandev.modest_meals.stamina.StaminaData;
import com.evandev.modest_meals.stamina.StaminaHolder;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements StaminaHolder, HealthRegenHolder {
    @Final
    @Shadow
    private Abilities abilities;

    @Unique
    private StaminaData mm$staminaData = StaminaData.create();

    @Unique
    private PlayerHealthRegen mm$healthRegen = new PlayerHealthRegen((Player) (Object) this);

    private PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public @NotNull StaminaData mm$getStaminaData() {
        return this.mm$staminaData;
    }

    @Override
    public void mm$setStaminaData(@NotNull StaminaData data) {
        this.mm$staminaData = data;
    }

    @Override
    public @NotNull PlayerHealthRegen mm$getHealthRegen() {
        return this.mm$healthRegen;
    }

    @Override
    public void mm$setHealthRegen(@NotNull PlayerHealthRegen regen) {
        this.mm$healthRegen = regen;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void mm$onAddAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        StaminaCodec.save((Player) (Object) this, compound);
        CompoundTag regenTag = new CompoundTag();
        this.mm$healthRegen.writeToNbt(regenTag);
        compound.put("ModestMealsHealthRegen", regenTag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void mm$onReadAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        StaminaCodec.read((Player) (Object) this, compound);
        if (compound.contains("ModestMealsHealthRegen")) {
            this.mm$healthRegen.readFromNbt(compound.getCompound("ModestMealsHealthRegen"));
        }
    }

    @WrapOperation(
            method = "eat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"
            )
    )
    private void mm$playerEatFood(FoodData foodData, FoodProperties foodProperties, Operation<Void> original) {
        if (!ModConfig.get().disableHunger) {
            original.call(foodData, foodProperties);
        }
    }

    @WrapMethod(method = "canEat")
    private boolean mm$canPlayerEatFood(boolean ignoreHunger, Operation<Boolean> original) {
        if (this.abilities.invulnerable || ignoreHunger) {
            return true;
        }
        if (ModConfig.get().disableHunger) {
            Player player = (Player) (Object) this;
            return FoodConsumption.canConsume(player, FoodValues.resolveHeldFood(player));
        }
        return original.call(ignoreHunger);
    }
}
