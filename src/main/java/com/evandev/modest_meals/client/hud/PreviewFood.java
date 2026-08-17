package com.evandev.modest_meals.client.hud;

import com.evandev.modest_meals.client.HoveredEdibleBlock;
import com.evandev.modest_meals.food.EdibleBlockFoods;
import com.evandev.modest_meals.food.FoodValues;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class PreviewFood {

    public static float healthPoints(Player player) {
        return scaled(player, FoodValues::healthPoints);
    }

    public static float staminaSeconds(Player player) {
        return scaled(player, FoodValues::staminaSeconds);
    }

    private static float scaled(Player player, java.util.function.ToDoubleFunction<ItemStack> value) {
        ItemStack held = FoodValues.resolveHeldFood(player);
        if (!held.isEmpty()) {
            return (float) value.applyAsDouble(held);
        }

        Optional<EdibleBlockFoods.EdibleBlock> block = HoveredEdibleBlock.get(player);
        if (block.isEmpty()) {
            return 0.0F;
        }
        EdibleBlockFoods.EdibleBlock bite = block.get();
        return (float) value.applyAsDouble(bite.stack()) * bite.biteScale();
    }
}
