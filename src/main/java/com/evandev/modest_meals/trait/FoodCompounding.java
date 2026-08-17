package com.evandev.modest_meals.trait;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.food.EdibleBlockFoods;
import com.evandev.modest_meals.trait.impl.*;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.*;

public class FoodCompounding {

    public static void apply(ItemStack result, RecipeInput input, boolean isCooking) {
        if (input == null || result == null || result.isEmpty()) return;
        List<ItemStack> inputs = new ArrayList<>(input.size());
        for (int i = 0; i < input.size(); i++) {
            ItemStack in = input.getItem(i);
            if (!in.isEmpty()) {
                inputs.add(in);
            }
        }
        apply(result, inputs, isCooking);
    }

    public static void apply(ItemStack result, List<ItemStack> inputs, boolean isCooking) {
        if (result == null || result.isEmpty() || inputs == null || inputs.isEmpty()) return;
        if (!isFood(result)) return;

        List<FoodTrait> compounded = compute(inputs, isCooking);
        if (!compounded.isEmpty()) {
            result.set(ModDataComponents.FOOD_TRAITS.get(), new FoodTraitsData(compounded));
            Constants.LOG.debug("Applied {} compounded traits to crafted {}", compounded.size(), result.getItem());
        }
    }

    public static boolean isFood(ItemStack stack) {
        return stack.has(DataComponents.FOOD)
                || EdibleBlockFoods.getFoodProperties(stack.getItem()).isPresent()
                || FoodTraitManager.hasTraits(stack);
    }

    public static List<FoodTrait> compute(List<ItemStack> inputs, boolean isCooking) {
        float valueMult = isCooking ? ModConfig.get().smeltingMultiplier : 1.0f;
        float durationMult = isCooking ? ModConfig.get().smeltingDurationMultiplier : 1.0f;

        Map<Object, FoodTrait> merged = new LinkedHashMap<>();

        for (ItemStack input : inputs) {
            if (input == null || input.isEmpty()) continue;
            List<FoodTrait> traits = FoodTraitManager.getTraits(input);
            for (FoodTrait trait : traits) {
                Object key = getMergeKey(trait);
                if (merged.containsKey(key)) {
                    merged.computeIfPresent(key, (k, existing) -> existing.compoundWith(trait, 1.0f, 1.0f));
                } else {
                    merged.put(key, trait);
                }
            }
        }

        if (merged.isEmpty()) {
            return Collections.emptyList();
        }

        List<FoodTrait> finalTraits = new ArrayList<>(merged.size());
        for (FoodTrait trait : merged.values()) {
            if (isCooking) {
                finalTraits.add(scaleWithCooking(trait, valueMult, durationMult));
            } else {
                finalTraits.add(trait);
            }
        }

        return finalTraits;
    }

    private static Object getMergeKey(FoodTrait trait) {
        return FoodTraitManager.getMergeKey(trait);
    }

    private static FoodTrait scaleWithCooking(FoodTrait trait, float valueMult, float durationMult) {
        if (trait instanceof EffectGrantTrait(
                Holder<MobEffect> effect, int duration, int amplifier,
                boolean showParticles, boolean ambient
        )) {
            return new EffectGrantTrait(effect, (int) (duration * durationMult), amplifier, showParticles, ambient);
        }
        if (trait instanceof StaminaRegenTrait(float value, int duration)) {
            return new StaminaRegenTrait(value * valueMult, (int) (duration * durationMult));
        }
        if (trait instanceof StaminaAdditionTrait(float value4)) {
            return new StaminaAdditionTrait(value4 * valueMult);
        }
        if (trait instanceof StaminaDepletionTrait(float value3, int duration4)) {
            return new StaminaDepletionTrait(value3 * valueMult, (int) (duration4 * durationMult));
        }
        if (trait instanceof StaminaNoRegenTrait(int duration3)) {
            return new StaminaNoRegenTrait((int) (duration3 * durationMult));
        }
        if (trait instanceof StaminaCapacityTrait(float value5, int duration5)) {
            return new StaminaCapacityTrait(value5 * valueMult, (int) (duration5 * durationMult));
        }
        if (trait instanceof HealthRegenTrait(float value2, int duration2)) {
            return new HealthRegenTrait(value2 * valueMult, (int) (duration2 * durationMult));
        }
        if (trait instanceof HealthAdditionTrait(float value1, int duration1)) {
            return new HealthAdditionTrait(value1 * valueMult, (int) (duration1 * durationMult));
        }
        if (trait instanceof HealthDepletionTrait(float value, int duration)) {
            return new HealthDepletionTrait(value * valueMult, (int) (duration * durationMult));
        }
        if (trait instanceof HealthNoRegenTrait(int duration)) {
            return new HealthNoRegenTrait((int) (duration * durationMult));
        }
        if (trait instanceof AirBubblesTrait(int value)) {
            return new AirBubblesTrait((int) (value * valueMult));
        }
        if (trait instanceof TeleportTrait(float range)) {
            return new TeleportTrait(range * valueMult);
        }
        return trait;
    }
}
