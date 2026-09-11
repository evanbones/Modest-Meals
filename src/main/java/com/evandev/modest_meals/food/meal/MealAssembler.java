package com.evandev.modest_meals.food.meal;

import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.food.FoodValues;
import com.evandev.modest_meals.food.ingredient.*;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.impl.HealthAdditionTrait;
import com.evandev.modest_meals.trait.impl.StaminaAdditionTrait;
import com.evandev.modest_meals.trait.impl.TemporaryHealthTrait;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * Turns a set of ingredients into a finished meal.
 */
public final class MealAssembler {

    /**
     * Work out what a meal made from these ingredients does.
     */
    public static Optional<MealContents> compute(List<ItemStack> ingredients, List<ItemStack> base) {
        return compute(ingredients, base, false);
    }

    public static Optional<MealContents> compute(List<ItemStack> ingredients, List<ItemStack> base, boolean dubious) {
        MealFormula formula = MealFormulaManager.get();

        List<Holder<Item>> used = new ArrayList<>();
        List<IngredientProfile> profiles = new ArrayList<>();
        List<ResourceLocation> ingredientIds = new ArrayList<>();
        for (ItemStack stack : ingredients) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            Optional<IngredientProfile> profile = IngredientProfileManager.resolve(stack);
            if (profile.isEmpty()) {
                if (!dubious) {
                    return Optional.empty();
                }
            } else {
                profiles.add(profile.get());
            }
            used.add(stack.getItem().builtInRegistryHolder());
            ingredientIds.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
        }
        if (used.isEmpty() || (profiles.isEmpty() && !dubious)) {
            return Optional.empty();
        }

        List<Holder<Item>> baseUsed = new ArrayList<>();
        List<IngredientProfile> baseProfiles = new ArrayList<>();
        List<ResourceLocation> baseIds = new ArrayList<>();
        for (ItemStack stack : base) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            IngredientProfileManager.resolve(stack).ifPresent(profile -> {
                baseProfiles.add(profile);
                baseUsed.add(stack.getItem().builtInRegistryHolder());
                baseIds.add(BuiltInRegistries.ITEM.getKey(stack.getItem()));
            });
        }

        float health = 0.0F;
        float stamina = 0.0F;
        int digestTicks = 0;
        float temporaryHealth = 0.0F;

        if (dubious) {
            for (ItemStack stack : concat(ingredients, base)) {
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                for (FoodTrait trait : FoodValues.effectiveTraits(stack)) {
                    if (trait instanceof HealthAdditionTrait(float value, int duration)) {
                        health += value;
                        digestTicks += duration;
                    } else if (trait instanceof StaminaAdditionTrait(float value)) {
                        stamina += value;
                    } else if (trait instanceof TemporaryHealthTrait(float value)) {
                        temporaryHealth += value;
                    }
                }
            }
        } else {
            for (IngredientProfile profile : concat(profiles, baseProfiles)) {
                health += profile.healthOrZero();
                stamina += profile.staminaOrZero();
                digestTicks += profile.digestTicksOrZero();
                temporaryHealth += profile.temporaryHealth();
            }
        }

        EffectResult effect = dubious
                ? EffectResult.NONE
                : computeEffect(formula, used, profiles, baseUsed, baseProfiles);

        MealContents contents = new MealContents(
                health,
                stamina,
                digestTicks,
                temporaryHealth,
                effect.effect(),
                effect.amplifier(),
                effect.durationTicks(),
                ingredientIds,
                baseIds,
                dubious
        );
        return contents.isMeaningful() ? Optional.of(contents) : Optional.empty();
    }

    /**
     * Build the finished stack for a meal type from these ingredients.
     */
    public static Optional<ItemStack> assemble(MealType type, List<ItemStack> ingredients, List<ItemStack> base) {
        Optional<Item> resultItem = type.resolveItem();
        if (resultItem.isEmpty()) {
            return Optional.empty();
        }
        int count = (int) ingredients.stream().filter(stack -> stack != null && !stack.isEmpty()).count();
        if (count < type.minIngredients() || count > type.maxIngredients()) {
            return Optional.empty();
        }

        for (MealType.ItemOverride override : type.itemOverrides()) {
            if (override.matches(ingredients)) {
                return BuiltInRegistries.ITEM.getOptional(override.result())
                        .map(item -> new ItemStack(item, override.count()));
            }
        }

        boolean hasUnsupported = false;
        if (type.requiresSupportedIngredients()) {
            for (ItemStack ingredient : ingredients) {
                if (ingredient != null && !ingredient.isEmpty()
                        && !MealIngredientManager.isSupported(type.id(), ingredient)) {
                    hasUnsupported = true;
                    break;
                }
            }
        }

        return compute(ingredients, base, hasUnsupported).map(contents -> {
            ItemStack result = new ItemStack(resultItem.get());
            result.set(ModDataComponents.MEAL_CONTENTS.get(), contents);
            return result;
        });
    }

    /**
     * Decide the meal's effect axis, tier and duration.
     */
    private static EffectResult computeEffect(MealFormula formula, List<Holder<Item>> used,
                                              List<IngredientProfile> profiles,
                                              List<Holder<Item>> baseUsed, List<IngredientProfile> baseProfiles) {
        Set<ResourceLocation> axes = new LinkedHashSet<>();
        for (IngredientProfile profile : profiles) {
            if (profile.hasEffect()) {
                axes.add(profile.effect().orElseThrow());
            }
        }
        if (axes.size() != 1) {
            return EffectResult.NONE;
        }

        ResourceLocation axisId = axes.iterator().next();
        Optional<MealEffect> maybeAxis = MealEffectManager.get(axisId);
        if (maybeAxis.isEmpty() || !maybeAxis.get().isUsable()) {
            return EffectResult.NONE;
        }
        MealEffect axis = maybeAxis.get();

        int potency = 0;
        int contributing = 0;
        for (IngredientProfile profile : profiles) {
            if (profile.hasEffect()) {
                potency += profile.potency();
                contributing++;
            }
        }

        Optional<Integer> amplifier = axis.amplifierFor(potency);
        if (amplifier.isEmpty()) {
            return EffectResult.NONE;
        }

        int seconds = axis.baseSeconds() * contributing
                + formula.secondsPerIngredient() * profiles.size()
                + bonusSeconds(concat(used, baseUsed), concat(profiles, baseProfiles));

        return new EffectResult(Optional.of(axisId), amplifier.get(), seconds * 20);
    }

    /**
     * Duration from time-boosting ingredients.
     */
    private static int bonusSeconds(List<Holder<Item>> used, List<IngredientProfile> profiles) {
        Set<ResourceLocation> counted = new HashSet<>();
        int bonus = 0;
        for (int i = 0; i < profiles.size(); i++) {
            IngredientProfile profile = profiles.get(i);
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(used.get(i).value());
            if (!counted.add(itemId)) {
                continue;
            }
            bonus += profile.timeBonusSeconds();
        }
        return bonus;
    }


    private static <T> List<T> concat(List<T> first, List<T> second) {
        List<T> all = new ArrayList<>(first);
        all.addAll(second);
        return all;
    }

    private record EffectResult(Optional<ResourceLocation> effect, int amplifier, int durationTicks) {
        static final EffectResult NONE = new EffectResult(Optional.empty(), 0, 0);
    }
}
