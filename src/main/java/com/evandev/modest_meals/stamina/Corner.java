package com.evandev.modest_meals.stamina;

import net.minecraft.network.chat.Component;

public enum Corner {
    TOP_LEFT("config.modest_meals.enum.corner.top_left"),
    TOP_RIGHT("config.modest_meals.enum.corner.top_right"),
    BOTTOM_LEFT("config.modest_meals.enum.corner.bottom_left"),
    BOTTOM_RIGHT("config.modest_meals.enum.corner.bottom_right");

    private final String key;

    Corner(String key) {
        this.key = key;
    }

    public Component getTitle() {
        return Component.translatable(this.key);
    }
}
