package com.evandev.modest_meals.stamina;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public interface StaminaHolder {
    @NotNull
    StaminaData mm$getStaminaData();

    void mm$setStaminaData(@NotNull StaminaData data);
}
