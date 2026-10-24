package com.evandev.modest_meals.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class PizzaItem extends BlockItem {

    public PizzaItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }

    @Override
    public Component getName(ItemStack stack) {
        return MealItem.nameOf(stack, Component.translatable(getDescriptionId(stack)));
    }
}
