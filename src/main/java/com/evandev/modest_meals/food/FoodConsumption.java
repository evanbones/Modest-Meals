package com.evandev.modest_meals.food;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.regen.HealthRegenHelper;
import com.evandev.modest_meals.stamina.StaminaHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Decides whether a player is allowed to eat a given food. A food may only be eaten when a bar it actually fills has room.
 */
public class FoodConsumption {

    public static boolean canConsume(Player player, ItemStack stack) {
        if (player.getAbilities().invulnerable) {
            return true;
        }

        if (!ModConfig.get().disableHunger) {
            return player.getFoodData().needsFood();
        }

        boolean touchesHealth = FoodValues.touchesHealth(stack);
        boolean touchesStamina = FoodValues.touchesStamina(stack);

        if (!touchesHealth && !touchesStamina) {
            return true;
        }
        if (touchesHealth && HealthRegenHelper.get(player).hasHealthRoom()) {
            return true;
        }
        return touchesStamina && StaminaHelper.get(player).hasStaminaRoom();
    }
}
