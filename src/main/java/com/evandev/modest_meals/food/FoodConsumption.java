package com.evandev.modest_meals.food;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.regen.HealthRegenHelper;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.evandev.modest_meals.trait.FoodTraitManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Decides whether a player is allowed to eat a given food. A food may only be eaten when a bar it actually fills has room.
 */
public class FoodConsumption {

    public static boolean canConsume(Player player, ItemStack stack) {
        if (player.getAbilities().invulnerable) {
            return true;
        }

        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null && food.canAlwaysEat()) {
            return true;
        }

        if (!ModConfig.get().disableHunger) {
            return player.getFoodData().needsFood();
        }

        if (FoodValues.hasUtility(stack)) {
            return true;
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

    public static void consumeFromBlock(Player player, int nutrition) {
        EdibleBlockContext.get(player).ifPresent(context -> {
            EdibleBlockFoods.EdibleBlock food = context.food();
            if (food != null) {
                FoodTraitManager.applyAll(player, food.stack(), food.biteScale());
            } else {
                applyFromNutrition(player, context.state(), nutrition);
            }
        });
    }

    private static void applyFromNutrition(Player player, BlockState state, int nutrition) {
        if (nutrition <= 0) {
            return;
        }
        ItemStack stack = state.getBlock().asItem().getDefaultInstance();
        FoodTraitManager.applyTraits(player, stack, FoodValues.effectiveTraits(stack, nutrition), 1.0F);
    }

    public static boolean canConsumeUnknown(Player player) {
        if (player.getAbilities().invulnerable) {
            return true;
        }

        if (!ModConfig.get().disableHunger) {
            return player.getFoodData().needsFood();
        }

        return HealthRegenHelper.get(player).hasHealthRoom() || StaminaHelper.get(player).hasStaminaRoom();
    }
}
