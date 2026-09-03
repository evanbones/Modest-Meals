package com.evandev.modest_meals.stamina;

import com.evandev.modest_meals.config.ModConfig;
import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;

public class StaminaData {
    public static final int MAX_STAMINA_LEVEL = 20;
    public static final Codec<StaminaData> CODEC = StaminaCodec.create();

    protected int stamina;
    protected int remaining;
    protected int cooldown;
    protected boolean exhausted;

    public StaminaData(int stamina, int remaining, int cooldown, boolean exhausted) {
        this.stamina = stamina;
        this.remaining = remaining;
        this.cooldown = cooldown;
        this.exhausted = exhausted;
    }

    public static StaminaData create() {
        int duration = ModConfig.get().staminaDuration * 20;
        int cooldown = ModConfig.get().staminaCooldown * 20;
        return new StaminaData(MAX_STAMINA_LEVEL, duration, cooldown, false);
    }

    public static int levelFor(int remaining, int ticks, int maxLevel) {
        if (ticks <= 0) {
            return 0;
        }
        return Mth.clamp((int) Math.ceil(((double) remaining / ticks) * maxLevel), 0, maxLevel);
    }

    public boolean isTiring(int maxLevel) {
        return this.stamina < maxLevel;
    }

    public int getStamina() {
        return stamina;
    }

    public void setStaminaRaw(int stamina) {
        this.stamina = Math.max(0, stamina);
    }

    public void setStaminaUsingTicks(int ticks, int maxLevel) {
        this.stamina = levelFor(this.remaining, ticks, maxLevel);
    }

    public int getRemaining() {
        return this.remaining;
    }

    public void setRemaining(int remainingInTicks) {
        this.remaining = Math.max(0, remainingInTicks);
    }

    public int getCooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = Mth.clamp(cooldown, 0, ModConfig.get().staminaCooldown * 20);
    }

    public boolean isExhausted() {
        return exhausted;
    }

    public void setExhausted(boolean exhausted) {
        this.exhausted = exhausted;
    }
}
