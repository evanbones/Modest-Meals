package com.evandev.modest_meals.food.meal;

import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.food.FoodProfileManager;
import com.evandev.modest_meals.food.ingredient.MealEffect;
import com.evandev.modest_meals.food.ingredient.MealEffectManager;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.impl.*;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Bridges a cooked meal's {@link MealContents} into its traits.
 */
public final class MealTraits {

    private MealTraits() {
    }

    public static List<FoodTrait> of(ItemStack stack, MealContents contents) {
        List<FoodTrait> traits = new ArrayList<>();

        if (contents.health() > 0.0F) {
            traits.add(new HealthAdditionTrait(contents.health(), digestTicksFor(stack, contents)));
        }
        if (contents.stamina() > 0.0F) {
            traits.add(new StaminaAdditionTrait(contents.stamina()));
        }
        if (contents.temporaryHealth() > 0.0F) {
            traits.add(new TemporaryHealthTrait(contents.temporaryHealth()));
        }

        if (contents.hasEffect()) {
            contents.effect()
                    .flatMap(MealEffectManager::get)
                    .flatMap(MealEffect::resolveMobEffect)
                    .ifPresent(effect -> traits.add(new EffectGrantTrait(
                            effect, contents.durationTicks(), contents.amplifier(), true, false)));
        }

        return traits;
    }

    /**
     * How long a meal's health takes to digest.
     */
    private static int digestTicksFor(ItemStack stack, MealContents contents) {
        if (contents.digestTicks() > 0) {
            return contents.digestTicks();
        }
        return FoodProfileManager.resolve(stack)
                .or(FoodProfileManager::resolveDefault)
                .map(profile -> profile.digestTicksFor(contents.health()))
                .orElse(0);
    }
}
