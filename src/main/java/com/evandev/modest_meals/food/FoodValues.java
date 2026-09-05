package com.evandev.modest_meals.food;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitManager;
import com.evandev.modest_meals.trait.FoodTraitType;
import com.evandev.modest_meals.trait.impl.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Source of truth for what a food restores. Everything that needs to know how many hearts or how
 * much stamina an item gives reads it from here.
 */
public class FoodValues {

    /**
     * The traits a food actually applies.
     */
    public static List<FoodTrait> effectiveTraits(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return List.of();
        }
        return withDerived(stack, FoodTraitManager.getTraits(stack),
                FoodProfileManager.resolve(stack).orElse(null),
                FoodTraitManager.suppressionsFor(stack.getItem()));
    }

    /**
     * The authored traits plus the health and stamina a food's profile gives it.
     */
    public static List<FoodTrait> withDerived(ItemStack stack, List<FoodTrait> authored,
                                              @Nullable FoodProfile profile, Set<String> suppressed) {
        boolean hasHealth = suppressed.contains(FoodTraitManager.mergeKeyOf(FoodTraitType.HEALTH_ADDITION));
        boolean hasStamina = suppressed.contains(FoodTraitManager.mergeKeyOf(FoodTraitType.STAMINA_ADDITION));
        for (FoodTrait trait : authored) {
            if (trait instanceof HealthAdditionTrait) {
                hasHealth = true;
            }
            if (trait instanceof StaminaAdditionTrait) {
                hasStamina = true;
            }
        }
        if (hasHealth && hasStamina) {
            return authored;
        }

        int nutrition = nutritionOf(stack);
        if (nutrition <= 0 || profile == null) {
            return authored;
        }

        List<FoodTrait> effective = new ArrayList<>(authored);
        if (!hasHealth) {
            float health = profile.healthFor(nutrition);
            if (health > 0.0F) {
                effective.add(new HealthAdditionTrait(health, profile.digestTicksFor(health)));
            }
        }
        if (!hasStamina) {
            float stamina = profile.staminaFor(nutrition);
            if (stamina > 0.0F) {
                effective.add(new StaminaAdditionTrait(stamina));
            }
        }
        return effective;
    }

    /**
     * Total health, in half-hearts, that eating this stack restores.
     */
    public static float healthPoints(ItemStack stack) {
        float total = 0.0F;
        for (FoodTrait trait : effectiveTraits(stack)) {
            if (trait instanceof HealthAdditionTrait healthAddition) {
                total += healthAddition.value();
            }
        }
        return total * ModConfig.get().traitGlobalValueMultiplier;
    }

    /**
     * How long, in ticks, the health from this stack takes to fully digest. Zero means it heals instantly.
     */
    public static int healthDigestTicks(ItemStack stack) {
        int longest = 0;
        for (FoodTrait trait : effectiveTraits(stack)) {
            if (trait instanceof HealthAdditionTrait healthAddition) {
                longest = Math.max(longest, healthAddition.duration());
            }
        }
        return (int) (longest * ModConfig.get().traitGlobalDurationMultiplier);
    }

    /**
     * Total stamina, in half-bolts, that eating this stack restores.
     */
    public static float staminaLevels(ItemStack stack) {
        float total = 0.0F;
        for (FoodTrait trait : effectiveTraits(stack)) {
            if (trait instanceof StaminaAdditionTrait(float value)) {
                total += value;
            }
        }
        return total * ModConfig.get().traitGlobalValueMultiplier;
    }

    public static boolean touchesHealth(ItemStack stack) {
        for (FoodTrait trait : effectiveTraits(stack)) {
            if (trait instanceof HealthAdditionTrait || trait instanceof HealthRegenTrait) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether this stack restores or regenerates stamina, and so should be gated on the stamina bar.
     */
    public static boolean touchesStamina(ItemStack stack) {
        if (!ModConfig.get().staminaSprint) {
            return false;
        }
        for (FoodTrait trait : effectiveTraits(stack)) {
            if (trait instanceof StaminaAdditionTrait || trait instanceof StaminaRegenTrait) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether this stack provides non-bar-filling utility traits (e.g. effects, cures, air, fire extinguishment...).
     */
    public static boolean hasUtility(ItemStack stack) {
        for (FoodTrait trait : effectiveTraits(stack)) {
            if (trait instanceof EffectGrantTrait
                    || trait instanceof EffectRemovalTrait
                    || trait instanceof FireExtinguishTrait
                    || trait instanceof AirBubblesTrait
                    || trait instanceof TeleportTrait
                    || trait instanceof StaminaCapacityTrait) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vanilla nutrition for this stack.
     */
    public static int nutritionOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null) {
            return food.nutrition();
        }
        return EdibleBlockFoods.getFoodProperties(stack.getItem())
                .map(FoodProperties::nutrition)
                .orElse(0);
    }

    /**
     * If something is a food (did you read the method name?).
     */
    public static boolean isFood(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.has(DataComponents.FOOD) || EdibleBlockFoods.getFoodProperties(stack.getItem()).isPresent();
    }

    /**
     * The food the player is holding (main hand first, then offhand).
     */
    public static ItemStack resolveHeldFood(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (isFood(mainHand)) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        if (isFood(offHand)) {
            return offHand;
        }
        return ItemStack.EMPTY;
    }
}
