package com.evandev.modest_meals.compat.farmers_delight;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.FoodValues;

import java.util.List;

public class FarmersDelightSoupEffects {

    public static List<FoodProperties.PossibleEffect> get(ItemStack stack) {
        Item item = stack.getItem();
        if (!Configuration.ENABLE_VANILLA_SOUP_EXTRA_EFFECTS.get()) {
            return List.of();
        }
        if (Configuration.ENABLE_RABBIT_STEW_BUFF.get() && item == Items.RABBIT_STEW) {
            return List.of();
        }
        FoodProperties soup = FoodValues.VANILLA_SOUP_EFFECTS.get(item);
        return soup == null ? List.of() : soup.effects();
    }
}
