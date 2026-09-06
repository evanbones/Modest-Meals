package com.evandev.modest_meals.stamina;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Storage hook for a player's stamina. Use {@link com.evandev.modest_meals.api.StaminaApi}.
 */
@ApiStatus.Internal
public interface StaminaHolder {
    @NotNull
    StaminaData mm$getStaminaData();

    void mm$setStaminaData(@NotNull StaminaData data);
}
