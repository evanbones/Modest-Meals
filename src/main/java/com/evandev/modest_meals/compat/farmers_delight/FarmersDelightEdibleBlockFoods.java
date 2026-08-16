package com.evandev.modest_meals.compat.farmers_delight;

import com.evandev.modest_meals.food.FoodPropertiesAdder;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import vectorwing.farmersdelight.common.block.FeastBlock;
import vectorwing.farmersdelight.common.block.PieBlock;
import vectorwing.farmersdelight.common.block.RiceRollMedleyBlock;

import java.util.Optional;

public class FarmersDelightEdibleBlockFoods {

    public static Optional<FoodProperties> getFoodProperties(BlockItem blockItem) {
        Block block = blockItem.getBlock();
        FoodPropertiesAdder totalFoodProperties = new FoodPropertiesAdder();
        switch (block) {
            case PieBlock pieBlock -> {
                totalFoodProperties.addFromItem(pieBlock.getPieSliceItem(), pieBlock.getMaxBites());
                return Optional.of(totalFoodProperties.getResult());
            }
            case RiceRollMedleyBlock riceRollMedleyBlock -> {
                riceRollMedleyBlock.riceRollServings.forEach(item -> totalFoodProperties.addFromItem(item.get().getDefaultInstance(), 1));
                return Optional.of(totalFoodProperties.getResult());
            }
            case FeastBlock feastBlock -> {
                totalFoodProperties.addFromItem(feastBlock.servingItem.get().getDefaultInstance(), feastBlock.getMaxServings());
                return Optional.of(totalFoodProperties.getResult());
            }
            default -> {
            }
        }
        return Optional.empty();
    }
}
