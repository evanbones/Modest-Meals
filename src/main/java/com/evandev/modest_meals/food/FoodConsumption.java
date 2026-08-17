package com.evandev.modest_meals.food;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.regen.HealthRegenHelper;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.evandev.modest_meals.trait.FoodTraitManager;
import com.evandev.modest_meals.trait.impl.HealthAdditionTrait;
import com.evandev.modest_meals.trait.impl.StaminaAdditionTrait;
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

    public static void consumeFromBlock(Player player, int nutrition) {
        EdibleBlockContext.get(player).ifPresent(context -> {
            EdibleBlockFoods.EdibleBlock food = context.food();
            if (food != null) {
                FoodTraitManager.applyAll(player, food.stack(), food.biteScale());
            } else {
                applyFromNutrition(player, nutrition);
            }
        });
    }

    private static void applyFromNutrition(Player player, int nutrition) {
        if (nutrition <= 0) {
            return;
        }
        FoodProfileManager.resolveDefault().ifPresent(profile -> {
            float valueMultiplier = ModConfig.get().traitGlobalValueMultiplier;
            float durationMultiplier = ModConfig.get().traitGlobalDurationMultiplier;

            float health = profile.healthFor(nutrition);
            if (health > 0.0F) {
                new HealthAdditionTrait(health, profile.digestTicksFor(health))
                        .apply(player, ItemStack.EMPTY, valueMultiplier, durationMultiplier);
            }

            float stamina = profile.staminaFor(nutrition);
            if (stamina > 0.0F) {
                new StaminaAdditionTrait(stamina).apply(player, ItemStack.EMPTY, valueMultiplier, durationMultiplier);
            }
        });
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
