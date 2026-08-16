package com.evandev.modest_meals.config;

import net.minecraft.network.chat.Component;

public enum HeartTextureOption {
    SINGLE_COLOR,
    ORIGINAL,
    BLINKING;

    public Component getTitle() {
        return Component.translatable("config.modest_meals.enum.heart_texture." + this.name().toLowerCase());
    }
}
