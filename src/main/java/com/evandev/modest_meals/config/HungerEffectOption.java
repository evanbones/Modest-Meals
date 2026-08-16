package com.evandev.modest_meals.config;

import net.minecraft.network.chat.Component;

public enum HungerEffectOption {
    VANILLA,
    DISABLED,
    REPLACED_WITH_OTHER;

    public Component getTitle() {
        return Component.translatable("config.modest_meals.enum.hunger_effect." + this.name().toLowerCase());
    }
}
