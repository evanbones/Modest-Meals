package com.evandev.modest_meals.food;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

//? if >=26.1 {
/*import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
*///?} else
import net.minecraft.world.food.FoodProperties;

public record FoodEffect(MobEffectInstance effect, float probability) {
    public static List<FoodEffect> ofStack(ItemStack stack) {
        List<FoodEffect> effects = new ArrayList<>();
        //? if >=26.1 {
        /*Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        if (consumable != null) {
            for (ConsumeEffect consumeEffect : consumable.onConsumeEffects()) {
                if (consumeEffect instanceof ApplyStatusEffectsConsumeEffect apply) {
                    apply.effects().forEach(effect -> effects.add(new FoodEffect(effect, apply.probability())));
                }
            }
        }
        *///?} else {
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null) {
            effects.addAll(of(food.effects()));
        }
        //?}
        return effects;
    }

    //? if <26.1 {
    public static List<FoodEffect> of(List<FoodProperties.PossibleEffect> effects) {
        return effects.stream().map(possible -> new FoodEffect(possible.effect(), possible.probability())).toList();
    }
    //?}
}
