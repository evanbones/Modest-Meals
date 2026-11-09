package com.evandev.modest_meals.food;

import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightCompat;
import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightSoupEffects;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitManager;
import com.evandev.modest_meals.trait.impl.EffectGrantTrait;
import net.minecraft.world.effect.MobEffectInstance;
//? if <26.1
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BuiltInFoodEffects {

    public static List<FoodEffect> of(ItemStack stack) {
        List<FoodEffect> effects = new ArrayList<>(FoodEffect.ofStack(stack));
        if (FarmersDelightCompat.isLoaded()) {
            effects.addAll(FarmersDelightSoupEffects.get(stack));
        }
        return effects;
    }

    public static boolean isKept(ItemStack stack, MobEffectInstance effect) {
        if (!ModConfig.get().foodEffects) {
            return false;
        }
        String key = FoodTraitManager.effectGrantKey(effect.getEffect());
        if (FoodTraitManager.suppressionsFor(stack.getItem()).contains(key)) {
            return false;
        }
        for (FoodTrait trait : FoodTraitManager.getTraits(stack)) {
            if (FoodTraitManager.getMergeKey(trait).equals(key)) {
                return false;
            }
        }
        return true;
    }

    public static List<FoodEffect> kept(ItemStack stack, List<FoodEffect> effects) {
        return effects.stream().filter(effect -> isKept(stack, effect.effect())).toList();
    }

    //? if <26.1 {
    public static FoodProperties withKeptEffects(ItemStack stack, FoodProperties food) {
        List<FoodProperties.PossibleEffect> kept = food.effects().stream()
                .filter(effect -> isKept(stack, effect.effect()))
                .toList();
        if (kept.size() == food.effects().size()) {
            return food;
        }
        return new FoodProperties(food.nutrition(), food.saturation(), food.canAlwaysEat(), food.eatSeconds(),
                food.usingConvertsTo(), kept);
    }
    //?}

    public static List<FoodTrait> appendTo(ItemStack stack, List<FoodTrait> traits, Set<String> suppressed) {
        if (stack.isEmpty()) {
            return traits;
        }
        List<FoodTrait> result = new ArrayList<>(traits);
        for (FoodEffect possible : of(stack)) {
            MobEffectInstance effect = possible.effect();
            String key = FoodTraitManager.effectGrantKey(effect.getEffect());
            if (suppressed.contains(key) || result.stream().anyMatch(t -> FoodTraitManager.getMergeKey(t).equals(key))) {
                continue;
            }
            result.add(new EffectGrantTrait(effect.getEffect(), effect.getDuration(), effect.getAmplifier(),
                    effect.isVisible(), effect.isAmbient(), 0, possible.probability()));
        }
        return result;
    }
}
