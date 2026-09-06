package com.evandev.modest_meals.config;

import net.minecraft.network.chat.Component;

public enum TooltipVisibility {
    VISIBLE,
    HIDDEN,
    SHIFT;

    public Component getTitle() {
        return Component.translatable("config.modest_meals.enum.tooltip_visibility." + this.name().toLowerCase());
    }
}
