package com.evandev.modest_meals.stamina;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.network.ClientboundStaminaSyncPayload;
import com.evandev.modest_meals.network.ModNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class PlayerStamina {
    public final Player player;
    public final StaminaData data;
    protected int durationInTicks;
    protected int cooldownInTicks;
    protected int rechargeInTicks;
    protected int halfRateInTicks;
    protected boolean tickAgain = false;
    protected PlayerStamina(Player player, StaminaData data) {
        this.player = player;
        this.data = data;

        this.durationInTicks = ModConfig.get().staminaDuration * 20;
        this.cooldownInTicks = ModConfig.get().staminaCooldown * 20;
        this.rechargeInTicks = ModConfig.get().staminaRecharge * 20;
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

    public void reset() {
        int duration = ModConfig.get().staminaDuration * 20;
        int cooldown = ModConfig.get().staminaCooldown * 20;
        int recharge = ModConfig.get().staminaRecharge * 20;

        this.durationInTicks = duration;
        this.cooldownInTicks = cooldown;
        this.rechargeInTicks = recharge;
        this.halfRateInTicks = 0;

        this.data.setCooldown(0);
        this.data.setRemaining(duration);
        this.data.setStaminaUsingTicks(duration);
        this.data.setExhausted(false);
    }

    public void tick() {
        Difficulty difficulty = this.player.level().getDifficulty();

        if (ModConfig.get().staminaInfinitePeaceful && Difficulty.PEACEFUL == difficulty) {
            this.data.setStaminaRaw(StaminaData.MAX_STAMINA_LEVEL);
            return;
        }

        if (this.data.isExhausted()) {
            if (this.isRegainable() && this.isTrackerNotHalved()) {
                this.data.remaining++;
            }

            if (this.data.remaining >= this.rechargeInTicks) {
                this.data.remaining = this.durationInTicks;
                this.data.cooldown = 0;
                this.data.setExhausted(false);

                if (this.player instanceof ServerPlayer serverPlayer) {
                    ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
                }
            }

            this.data.setStaminaUsingTicks(this.data.isExhausted() ? this.rechargeInTicks : this.durationInTicks);
        } else if (this.isAtFullSprint()) {
            this.data.remaining--;
            this.data.cooldown = this.cooldownInTicks;

            if (this.data.remaining <= 0) {
                this.data.remaining = 0;
                this.data.setExhausted(true);

                if (this.player instanceof ServerPlayer serverPlayer) {
                    ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
                }
            }

            this.data.setStaminaUsingTicks(this.durationInTicks);
        } else {
            if (this.data.remaining < this.durationInTicks) {
                if (this.data.cooldown <= 0 && this.isRegainable() && this.isTrackerNotHalved()) {
                    this.data.remaining++;
                } else if (this.data.cooldown > 0) {
                    this.data.cooldown--;
                }
            }

            this.data.setStaminaUsingTicks(this.durationInTicks);
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
        return this.data.isTiring() || this.data.isExhausted() || this.isAtFullSprint();
    }

    public boolean isCoolingDown() {
        if (!ModConfig.get().staminaSprint) {
            return false;
        }
        return this.data.cooldown > 0 && this.data.cooldown < this.cooldownInTicks;
    }

    public boolean isRegainable() {
        if (ModConfig.get().staminaRegainWhenMoving != StaminaRegain.NONE) {
            return true;
        }
        return !this.isMoving();
    }

    public boolean isNotRegainable() {
        return !this.isRegainable() && !this.isAtFullSprint() && this.data.isTiring();
    }

    public boolean isTrackerNotHalved() {
        boolean isHalved = false;
        boolean isHungry = false;

        if (ModConfig.get().staminaRegainWhenMoving == StaminaRegain.HALF && this.isMoving()) {
            isHalved = !this.isAtFullSprint() && this.data.isTiring();
        }

        if (ModConfig.get().staminaHungerEffect) {
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
        if (ModConfig.get().staminaSaturationEffect && this.isTiringOrExhausted()) {
            return this.player.hasEffect(MobEffects.SATURATION) && !this.isAtFullSprint();
        }
        return false;
    }

    public boolean hasNegativeEffect() {
        if (ModConfig.get().staminaHungerEffect && this.isTiringOrExhausted()) {
            return this.player.hasEffect(MobEffects.HUNGER);
        }
        return false;
    }
}
