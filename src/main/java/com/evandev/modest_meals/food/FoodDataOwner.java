package com.evandev.modest_meals.food;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface FoodDataOwner {
    @Nullable
    Player mm$getOwner();

    void mm$setOwner(Player player);
}
