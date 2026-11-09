package com.evandev.modest_meals.stamina;

import com.evandev.modest_meals.api.StaminaEvent;
import com.evandev.modest_meals.attribute.ModAttributes;
import com.evandev.modest_meals.compat.reliable_gliders.ReliableGlidersCompat;
import com.evandev.modest_meals.compat.shield_expansion.ShieldExpansionCompat;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.effect.ModMobEffects;
import com.evandev.modest_meals.mixin.MobEffectInstanceAccessor;
import com.evandev.modest_meals.network.ClientboundStaminaSyncPayload;
import com.evandev.modest_meals.network.ModNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
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
        return this.barTicks(this.data.isExhausted());
    }

    private static boolean locksOut() {
        return ModConfig.get().staminaExhaustionLockout;
    }

    private int barTicks(boolean exhausted) {
        return exhausted && locksOut() ? this.getRechargeInTicks() : this.getDurationInTicks();
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
        this.data.setStaminaUsingTicks(this.barTicks(gain.exhausted()), this.getMaxLevel());
    }

    public void spend(float levels) {
        int ticks = this.levelsToTicks(levels);
        if (ticks <= 0) {
            return;
        }

        int remaining = this.data.getRemaining() - ticks;
        if (remaining <= 0) {
            this.data.setRemaining(0);
            this.markExhausted();
        } else {
            this.data.setRemaining(remaining);
        }
        this.data.setStaminaUsingTicks(this.getActiveBarInTicks(), this.getMaxLevel());
    }

    private void markExhausted() {
        if (this.data.isExhausted() || this.player.level().isClientSide()) {
            return;
        }
        this.data.setExhausted(true);
        if (!locksOut()) {
            this.data.setCooldown(this.getCooldownInTicks());
        }
        if (!this.player.level().isClientSide()) {
            StaminaEvent.postExhausted(this.player);
        }
        this.syncNow();
    }

    public int levelAfterGain(float levels) {
        Gain gain = gain(levels);
        return StaminaData.levelFor(gain.remaining(),
                this.barTicks(gain.exhausted()),
                this.getMaxLevel());
    }

    private Gain gain(float levels) {
        int newRemaining = this.data.getRemaining() + this.levelsToTicks(levels);

        if (this.data.isExhausted() && !locksOut()) {
            int remaining = Math.min(this.getDurationInTicks(), newRemaining);
            return new Gain(remaining, remaining <= 0);
        }
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
        if (!this.isExerting()) {
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

        if (this.data.isExhausted() && !locksOut()) {
            if (this.data.cooldown > 0) {
                this.data.cooldown--;
            } else if (!this.player.level().isClientSide()) {
                this.recover();
            }
            this.data.setStaminaUsingTicks(durationInTicks, maxLevel);
        } else if (this.data.isExhausted()) {
            if (this.regainThisTick) {
                this.data.remaining++;
            }

            if (this.data.remaining >= rechargeInTicks) {
                if (this.player.level().isClientSide()) {
                    this.data.remaining = rechargeInTicks;
                } else {
                    this.data.remaining = durationInTicks;
                    this.data.cooldown = 0;
                    this.recover();
                }
            }

            this.data.setStaminaUsingTicks(this.barTicks(this.data.isExhausted()), maxLevel);
        } else if (this.isExerting()) {
            if (!this.isNourished() && !this.isDrainCancelled()) {
                this.data.remaining--;

                if (this.data.remaining <= 0) {
                    this.data.remaining = 0;
                    this.markExhausted();
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
                this.data.remaining = durationInTicks;
            }

            this.data.setStaminaUsingTicks(durationInTicks, maxLevel);
        }
    }

    private void recover() {
        this.data.setExhausted(false);
        if (!this.player.level().isClientSide()) {
            StaminaEvent.postRecovered(this.player);
        }
        this.syncNow();
    }

    private boolean isDrainCancelled() {
        if (this.player.level().isClientSide()) {
            return false;
        }
        return StaminaEvent.postDrain(this.player);
    }

    public void syncNow() {
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
        return ModConfig.get().staminaSprinting && !this.player.isUsingItem() && !this.player.isPassenger() && this.player.isSprinting();
    }

    public boolean isElytraGliding() {
        return ModConfig.get().staminaElytra && this.player.isFallFlying();
    }

    public boolean isExerting() {
        if (this.isAtFullSprint()) {
            return true;
        }
        if (this.player.level().isClientSide() && !this.player.isLocalPlayer()) {
            return false;
        }
        return this.isElytraGliding() || ReliableGlidersCompat.isGliding(this.player) || ShieldExpansionCompat.isBlocking(this.player);
    }

    public boolean isTiringOrExhausted() {
        if (!ModConfig.get().staminaSprint) {
            return false;
        }
        return this.data.isTiring(this.getMaxLevel()) || this.data.isExhausted() || this.isExerting();
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

    public void holdEnduranceWhileNourished() {
        if (!this.isNourished()) {
            return;
        }
        MobEffectInstance endurance = this.player.getEffect(ModMobEffects.STAMINA_BOOST);
        if (endurance != null && !endurance.isInfiniteDuration()) {
            ((MobEffectInstanceAccessor) endurance).mm$setDuration(endurance.getDuration() + 1);
        }
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
        return !this.isRegainable() && !this.isExerting() && this.data.isTiring(this.getMaxLevel());
    }

    public boolean isTrackerNotHalved() {
        boolean isHalved = false;

        if (ModConfig.get().staminaRegainWhenMoving == StaminaRegain.HALF && this.isMoving()) {
            isHalved = !this.isExerting() && this.data.isTiring(this.getMaxLevel());
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
        if (!(this.player.level() instanceof ServerLevel serverLevel)
                || !serverLevel.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)) {
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
