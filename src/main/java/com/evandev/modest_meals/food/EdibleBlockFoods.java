package com.evandev.modest_meals.food;

import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightCompat;
import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightEdibleBlockFoods;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.CakeBlock;

import java.util.Optional;

public class EdibleBlockFoods {
    public static final FoodProperties CAKE_PROPERTIES = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build();

    public static Optional<FoodProperties> getFoodProperties(Item item) {
        if (item instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof CakeBlock) {
                FoodPropertiesAdder foodPropertiesAdder = new FoodPropertiesAdder();
                foodPropertiesAdder.add(CAKE_PROPERTIES, CakeBlock.MAX_BITES + 1);
                return Optional.of(foodPropertiesAdder.getResult());
            }
            if (FarmersDelightCompat.isLoaded()) {
                return FarmersDelightEdibleBlockFoods.getFoodProperties(blockItem);
            }
        }
        return Optional.empty();
    }
}
