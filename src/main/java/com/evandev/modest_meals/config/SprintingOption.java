package com.evandev.modest_meals.config;

import net.minecraft.network.chat.Component;

public enum SprintingOption {
    VANILLA,
    DISABLED,
    LIMITED_BY_HEALTH;

    public Component getTitle() {
        return Component.translatable("config.modest_meals.enum.sprinting." + this.name().toLowerCase());
    }
}
