package com.evandev.modest_meals.config;

import net.minecraft.network.chat.Component;

public enum RegenerationAtFullHealthOption {
    STOPPED,
    CONTINUED,
    STORED;

    public Component getTitle() {
        return Component.translatable("config.modest_meals.enum.regeneration_at_full_health." + this.name().toLowerCase());
    }
}
