package com.evandev.modest_meals.food;

import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightCompat;
import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightSoupEffects;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitManager;
import com.evandev.modest_meals.trait.impl.EffectGrantTrait;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BuiltInFoodEffects {

    public static List<FoodProperties.PossibleEffect> of(ItemStack stack) {
        List<FoodProperties.PossibleEffect> effects = new ArrayList<>();
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null) {
            effects.addAll(food.effects());
        }
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

    public static List<FoodProperties.PossibleEffect> kept(ItemStack stack, List<FoodProperties.PossibleEffect> effects) {
        return effects.stream().filter(effect -> isKept(stack, effect.effect())).toList();
    }

    public static FoodProperties withKeptEffects(ItemStack stack, FoodProperties food) {
        List<FoodProperties.PossibleEffect> kept = kept(stack, food.effects());
        if (kept.size() == food.effects().size()) {
            return food;
        }
        return new FoodProperties(food.nutrition(), food.saturation(), food.canAlwaysEat(), food.eatSeconds(),
                food.usingConvertsTo(), kept);
    }

    public static List<FoodTrait> appendTo(ItemStack stack, List<FoodTrait> traits, Set<String> suppressed) {
        if (stack.isEmpty()) {
            return traits;
        }
        List<FoodTrait> result = new ArrayList<>(traits);
        for (FoodProperties.PossibleEffect possible : of(stack)) {
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
