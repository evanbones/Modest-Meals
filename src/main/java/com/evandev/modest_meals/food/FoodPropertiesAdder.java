package com.evandev.modest_meals.food;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

public class FoodPropertiesAdder {
    private int nutrition = 0;
    private float saturation = 0.0F;

    public void add(FoodProperties foodProperties, int multiplier) {
        if (foodProperties == null) return;
        nutrition += foodProperties.nutrition() * multiplier;
        saturation += foodProperties.saturation() * multiplier;
    }

    public void addFromItem(ItemStack item, int multiplier) {
        if (item == null) return;
        FoodProperties foodProperties = item.get(DataComponents.FOOD);
        add(foodProperties, multiplier);
    }

    public FoodProperties getResult() {
        float saturationModifier = nutrition > 0 ? (saturation / (nutrition * 2.0F)) : 0.0F;
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturationModifier).build();
    }
}
