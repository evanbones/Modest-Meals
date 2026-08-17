package com.evandev.modest_meals.stamina;

import com.evandev.modest_meals.attribute.ModAttributes;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.effect.ModMobEffects;
import com.evandev.modest_meals.network.ClientboundStaminaSyncPayload;
import com.evandev.modest_meals.network.ModNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class PlayerStamina {
    public final Player player;
    public final StaminaData data;
    protected int halfRateInTicks;
    protected boolean tickAgain = false;

    protected PlayerStamina(Player player, StaminaData data) {
        this.player = player;
        this.data = data;
        this.halfRateInTicks = 0;
    }

    public static PlayerStamina create(Player player) {
        if (player instanceof StaminaHolder holder) {
            return new PlayerStamina(player, holder.mm$getStaminaData());
        } else {
            throw new IllegalStateException("Player instance is not a holder of stamina data");
        }
    }

    public Player getPlayer() {
        return this.player;
    }

    public StaminaData getData() {
        return this.data;
    }

    public int getMaxLevel() {
        double capacity = this.player.getAttributes().hasAttribute(ModAttributes.STAMINA_CAPACITY)
                ? this.player.getAttributeValue(ModAttributes.STAMINA_CAPACITY)
                : StaminaData.MAX_STAMINA_LEVEL;
        return Mth.clamp(
                (int) capacity,
                (int) ModAttributes.MIN_STAMINA_CAPACITY,
                (int) ModAttributes.MAX_STAMINA_CAPACITY
        );
    }

    public int getDurationInTicks() {
        return scaleToCapacity(ModConfig.get().staminaDuration * 20);
    }

    public int getRechargeInTicks() {
        return scaleToCapacity(ModConfig.get().staminaRecharge * 20);
    }

    public int getCooldownInTicks() {
        return ModConfig.get().staminaCooldown * 20;
    }

    private int scaleToCapacity(int baseTicks) {
        return Math.max(1, (int) ((long) baseTicks * getMaxLevel() / (double) StaminaData.MAX_STAMINA_LEVEL));
    }

    public void reset() {
        int duration = getDurationInTicks();

        this.halfRateInTicks = 0;

        this.data.setCooldown(0);
        this.data.setRemaining(duration);
        this.data.setStaminaUsingTicks(duration, getMaxLevel());
        this.data.setExhausted(false);
    }

    public void tick() {
        Difficulty difficulty = this.player.level().getDifficulty();
        int maxLevel = this.getMaxLevel();

        if (ModConfig.get().staminaInfinitePeaceful && Difficulty.PEACEFUL == difficulty) {
            this.data.setStaminaRaw(maxLevel);
            return;
        }

        int durationInTicks = this.getDurationInTicks();
        int rechargeInTicks = this.getRechargeInTicks();

        if (this.data.isExhausted()) {
            if (this.isRegainable() && this.isTrackerNotHalved()) {
                this.data.remaining++;
            }

            if (this.data.remaining >= rechargeInTicks) {
                this.data.remaining = durationInTicks;
                this.data.cooldown = 0;
                this.data.setExhausted(false);

                if (this.player instanceof ServerPlayer serverPlayer) {
                    ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
                }
            }

            this.data.setStaminaUsingTicks(this.data.isExhausted() ? rechargeInTicks : durationInTicks, maxLevel);
        } else if (this.isAtFullSprint()) {
            this.data.remaining--;
            this.data.cooldown = this.getCooldownInTicks();

            if (this.data.remaining <= 0) {
                this.data.remaining = 0;
                this.data.setExhausted(true);

                if (this.player instanceof ServerPlayer serverPlayer) {
                    ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
                }
            }

            this.data.setStaminaUsingTicks(durationInTicks, maxLevel);
        } else {
            if (this.data.remaining < durationInTicks) {
                if (this.data.cooldown <= 0 && this.isRegainable() && this.isTrackerNotHalved()) {
                    this.data.remaining++;
                } else if (this.data.cooldown > 0) {
                    this.data.cooldown--;
                }
            } else if (this.data.remaining > durationInTicks) {
                // The capacity attribute or the config shrank underneath us.
                this.data.remaining = durationInTicks;
            }

            this.data.setStaminaUsingTicks(durationInTicks, maxLevel);
        }

        boolean shouldTickAgain = this.hasPositiveEffect();

        if (this.hasNegativeEffect() && this.isAtFullSprint()) {
            shouldTickAgain = true;
        }

        if (shouldTickAgain && !this.tickAgain) {
            this.tickAgain = true;
            this.tick();
        } else {
            this.tickAgain = false;
        }
    }

    public boolean isMoving() {
        double dx = this.player.getKnownMovement().x();
        double dz = this.player.getKnownMovement().z();
        return dx * dx + dz * dz > 2.5E-5F;
    }

    public boolean isAtFullSprint() {
        return !this.player.isUsingItem() && this.player.isSprinting();
    }

    public boolean isTiringOrExhausted() {
        if (!ModConfig.get().staminaSprint) {
            return false;
        }
        return this.data.isTiring(this.getMaxLevel()) || this.data.isExhausted() || this.isAtFullSprint();
    }

    public boolean hasStaminaRoom() {
        return this.data.isExhausted() || this.data.getRemaining() < this.getDurationInTicks();
    }

    public boolean isCoolingDown() {
        if (!ModConfig.get().staminaSprint) {
            return false;
        }
        return this.data.cooldown > 0 && this.data.cooldown < this.getCooldownInTicks();
    }

    public boolean isRegainable() {
        if (this.player.hasEffect(ModMobEffects.STAMINA_NO_REGEN)) {
            return false;
        }
        if (ModConfig.get().staminaRegainWhenMoving != StaminaRegain.NONE) {
            return true;
        }
        return !this.isMoving();
    }

    public boolean isNotRegainable() {
        return !this.isRegainable() && !this.isAtFullSprint() && this.data.isTiring(this.getMaxLevel());
    }

    public boolean isTrackerNotHalved() {
        boolean isHalved = false;
        boolean isHungry = false;

        if (ModConfig.get().staminaRegainWhenMoving == StaminaRegain.HALF && this.isMoving()) {
            isHalved = !this.isAtFullSprint() && this.data.isTiring(this.getMaxLevel());
        }

        if (this.player.hasEffect(ModMobEffects.STAMINA_DEPLETION)) {
            isHungry = true;
        } else if (ModConfig.get().staminaHungerEffect) {
            isHungry = this.player.hasEffect(MobEffects.HUNGER);
        }

        if (isHalved || isHungry) {
            if (this.halfRateInTicks >= 1) {
                this.halfRateInTicks = 0;
            } else {
                this.halfRateInTicks++;
                return false;
            }
        } else {
            this.halfRateInTicks = 0;
        }

        return true;
    }

    public boolean hasPositiveEffect() {
        if (this.isTiringOrExhausted()) {
            if (this.player.hasEffect(ModMobEffects.STAMINA_REGEN) && !this.isAtFullSprint()) {
                return true;
            }
            return ModConfig.get().staminaSaturationEffect && this.player.hasEffect(MobEffects.SATURATION) && !this.isAtFullSprint();
        }
        return false;
    }

    public boolean hasNegativeEffect() {
        if (this.isTiringOrExhausted()) {
            if (this.player.hasEffect(ModMobEffects.STAMINA_DEPLETION)) {
                return true;
            }
            return ModConfig.get().staminaHungerEffect && this.player.hasEffect(MobEffects.HUNGER);
        }
        return false;
    }
}
