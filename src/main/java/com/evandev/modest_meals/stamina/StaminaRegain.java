package com.evandev.modest_meals.stamina;

import net.minecraft.network.chat.Component;

public enum StaminaRegain {
    NORMAL("config.modest_meals.enum.stamina_regain.normal"),
    HALF("config.modest_meals.enum.stamina_regain.half"),
    NONE("config.modest_meals.enum.stamina_regain.none");

    private final String key;

    StaminaRegain(String key) {
        this.key = key;
    }

    public Component getTitle() {
        return Component.translatable(this.key);
    }
}
