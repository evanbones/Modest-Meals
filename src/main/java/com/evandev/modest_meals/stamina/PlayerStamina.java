package com.evandev.modest_meals.stamina;

import com.evandev.modest_meals.attribute.ModAttributes;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.effect.ModMobEffects;
import com.evandev.modest_meals.network.ClientboundStaminaSyncPayload;
import com.evandev.modest_meals.network.ModNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

public class PlayerStamina {
    private static final int SYNC_INTERVAL_TICKS = 20;

    public final Player player;
    public final StaminaData data;
    protected int halfRateInTicks;
    protected boolean regainThisTick = false;
    private int ticksSinceSync = 0;
    private int naturalRegenTickTimer = 0;

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

    public int getActiveBarInTicks() {
        return this.data.isExhausted() ? this.getRechargeInTicks() : this.getDurationInTicks();
    }

    public int levelsToTicks(float levels) {
        int maxLevel = this.getMaxLevel();
        if (maxLevel <= 0) {
            return 0;
        }
        return Math.round(levels * this.getActiveBarInTicks() / (float) maxLevel);
    }

    public void addLevels(float levels) {
        Gain gain = gain(levels);
        this.data.setRemaining(gain.remaining());
        this.data.setExhausted(gain.exhausted());
        this.data.setStaminaUsingTicks(gain.exhausted() ? this.getRechargeInTicks() : this.getDurationInTicks(), this.getMaxLevel());
    }

    public int levelAfterGain(float levels) {
        Gain gain = gain(levels);
        return StaminaData.levelFor(gain.remaining(),
                gain.exhausted() ? this.getRechargeInTicks() : this.getDurationInTicks(),
                this.getMaxLevel());
    }

    private Gain gain(float levels) {
        int newRemaining = this.data.getRemaining() + this.levelsToTicks(levels);

        if (this.data.isExhausted()) {
            if (newRemaining >= this.getRechargeInTicks()) {
                return new Gain(this.getDurationInTicks(), false);
            }
            return new Gain(newRemaining, true);
        }
        return new Gain(Math.min(this.getDurationInTicks(), newRemaining), false);
    }

    private int scaleToCapacity(int baseTicks) {
        return Math.max(1, (int) ((long) baseTicks * getMaxLevel() / (double) StaminaData.MAX_STAMINA_LEVEL));
    }

    public void reset() {
        int duration = getDurationInTicks();

        this.halfRateInTicks = 0;
        this.naturalRegenTickTimer = 0;

        this.data.setCooldown(0);
        this.data.setRemaining(duration);
        this.data.setStaminaUsingTicks(duration, getMaxLevel());
        this.data.setExhausted(false);
    }

    public void tick() {
        Difficulty difficulty = this.player.level().getDifficulty();
        int maxLevel = this.getMaxLevel();

        if (ModConfig.get().staminaInfinitePeaceful && Difficulty.PEACEFUL == difficulty) {
            this.halfRateInTicks = 0;
            this.data.setRemaining(this.getDurationInTicks());
            this.data.setCooldown(0);
            this.data.setExhausted(false);
            this.data.setStaminaRaw(maxLevel);
            return;
        }

        this.regainThisTick = this.isRegainable() && this.isTrackerNotHalved();

        int extraSteps = 0;
        if (!this.isAtFullSprint()) {
            int posAmp = this.getPositiveEffectAmplifier();
            if (posAmp >= 0) {
                extraSteps += (posAmp + 1);
            }
        } else {
            int negAmp = this.getNegativeEffectAmplifier();
            if (negAmp >= 0) {
                extraSteps += (negAmp + 1);
            }
        }

        this.step(maxLevel);
        for (int i = 0; i < extraSteps; i++) {
            this.step(maxLevel);
        }

        this.syncPeriodically();
    }

    private void step(int maxLevel) {
        int durationInTicks = this.getDurationInTicks();
        int rechargeInTicks = this.getRechargeInTicks();

        if (this.data.isExhausted()) {
            if (this.regainThisTick) {
                this.data.remaining++;
            }

            if (this.data.remaining >= rechargeInTicks) {
                this.data.remaining = durationInTicks;
                this.data.cooldown = 0;
                this.data.setExhausted(false);
                this.syncNow();
            }

            this.data.setStaminaUsingTicks(this.data.isExhausted() ? rechargeInTicks : durationInTicks, maxLevel);
        } else if (this.isAtFullSprint()) {
            if (!this.isNourished()) {
                this.data.remaining--;

                if (this.data.remaining <= 0) {
                    this.data.remaining = 0;
                    this.data.setExhausted(true);
                    this.syncNow();
                }
            }
            this.data.cooldown = this.getCooldownInTicks();

            this.data.setStaminaUsingTicks(durationInTicks, maxLevel);
        } else {
            if (this.data.remaining < durationInTicks) {
                if (this.data.cooldown > 0) {
                    this.data.cooldown--;
                } else if (this.regainThisTick) {
                    this.data.remaining++;
                }
            } else if (this.data.remaining > durationInTicks) {
                // The capacity attribute or the config shrank underneath us.
                this.data.remaining = durationInTicks;
            }

            this.data.setStaminaUsingTicks(durationInTicks, maxLevel);
        }
    }

    private void syncNow() {
        if (this.player instanceof ServerPlayer serverPlayer) {
            this.ticksSinceSync = 0;
            ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
        }
    }

    private void syncPeriodically() {
        if (!(this.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (++this.ticksSinceSync < SYNC_INTERVAL_TICKS) {
            return;
        }
        this.ticksSinceSync = 0;
        ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
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

    public boolean isNourished() {
        return this.player.hasEffect(ModMobEffects.STAMINA_NOURISHMENT);
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

        if (ModConfig.get().staminaRegainWhenMoving == StaminaRegain.HALF && this.isMoving()) {
            isHalved = !this.isAtFullSprint() && this.data.isTiring(this.getMaxLevel());
        }

        if (isHalved || this.hasNegativeEffect()) {
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
        return getPositiveEffectAmplifier() >= 0;
    }

    public int getPositiveEffectAmplifier() {
        MobEffectInstance regen = this.player.getEffect(ModMobEffects.STAMINA_REGEN);
        if (regen != null) {
            return regen.getAmplifier();
        }
        MobEffectInstance sat = this.player.getEffect(MobEffects.SATURATION);
        if (sat != null) {
            return sat.getAmplifier();
        }
        return -1;
    }

    public boolean hasNegativeEffect() {
        return getNegativeEffectAmplifier() >= 0;
    }

    public int getNegativeEffectAmplifier() {
        MobEffectInstance deplet = this.player.getEffect(ModMobEffects.STAMINA_DEPLETION);
        if (deplet != null) {
            return deplet.getAmplifier();
        }
        MobEffectInstance hunger = this.player.getEffect(MobEffects.HUNGER);
        if (hunger != null) {
            return hunger.getAmplifier();
        }
        return -1;
    }

    public void tickNaturalRegeneration() {
        if (!ModConfig.get().staminaNaturalRegeneration) {
            this.naturalRegenTickTimer = 0;
            return;
        }
        if (this.player.level().isClientSide()) {
            return;
        }
        boolean naturalRegenRule = this.player.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
        if (!naturalRegenRule) {
            this.naturalRegenTickTimer = 0;
            return;
        }
        if (this.player.hasEffect(ModMobEffects.HEALTH_NO_REGEN)) {
            this.naturalRegenTickTimer = 0;
            return;
        }
        if (this.data.isExhausted() || this.player.hasEffect(ModMobEffects.STAMINA_NO_REGEN)) {
            this.naturalRegenTickTimer = 0;
            return;
        }
        if (!this.player.isHurt() || this.player.getHealth() >= this.player.getMaxHealth()) {
            this.naturalRegenTickTimer = 0;
            return;
        }
        int currentStamina = this.data.getStamina();
        int maxLevel = this.getMaxLevel();
        if (currentStamina < ModConfig.get().staminaNaturalRegenerationThreshold) {
            this.naturalRegenTickTimer = 0;
            return;
        }

        boolean isFullStamina = currentStamina >= maxLevel;
        int interval = isFullStamina
                ? Math.max(1, ModConfig.get().staminaNaturalRegenerationFastInterval)
                : Math.max(1, ModConfig.get().staminaNaturalRegenerationInterval);

        this.naturalRegenTickTimer++;
        if (this.naturalRegenTickTimer >= interval) {
            this.naturalRegenTickTimer = 0;
            this.player.heal(1.0F);

            float drain = ModConfig.get().staminaNaturalRegenerationDrain;
            if (drain > 0.0F) {
                int drainTicks = this.levelsToTicks(drain);
                if (drainTicks > 0) {
                    this.data.remaining = Math.max(0, this.data.remaining - drainTicks);
                    int durationInTicks = this.getDurationInTicks();
                    this.data.setStaminaUsingTicks(durationInTicks, maxLevel);
                    this.syncNow();
                }
            }
        }
    }

    private record Gain(int remaining, boolean exhausted) {
    }
}
