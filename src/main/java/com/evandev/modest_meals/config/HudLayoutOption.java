package com.evandev.modest_meals.config;

import net.minecraft.network.chat.Component;

public enum HudLayoutOption {
    DEFAULT,
    CLASSIC;

    public Component getTitle() {
        return Component.translatable("config.modest_meals.enum.hud_layout." + this.name().toLowerCase());
    }
}
