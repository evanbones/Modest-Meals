package com.evandev.modest_meals.food;

import com.evandev.modest_meals.block.SlicedMealBlock;
import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightCompat;
import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightEdibleBlockFoods;
import com.evandev.modest_meals.item.MealItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Optional;

public class EdibleBlockFoods {
    public static final FoodProperties CAKE_PROPERTIES = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build();
    private static final String[] BITE_PROPERTY_NAMES = {"bites", "servings", "slices", "uses"};

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

    public static Optional<Servings> servingsOf(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return Optional.empty();
        }
        Block block = blockItem.getBlock();
        if (block instanceof SlicedMealBlock meal) {
            MealContents contents = MealItem.contentsOf(stack);
            return contents.isMeaningful()
                    ? Optional.of(Servings.slices(meal.sliceOf(contents), 1.0F, meal.slices()))
                    : Optional.empty();
        }
        if (block instanceof CakeBlock) {
            int slices = CakeBlock.MAX_BITES + 1;
            return Optional.of(Servings.slices(stack, 1.0F / slices, slices));
        }
        if (FarmersDelightCompat.isLoaded()) {
            return FarmersDelightEdibleBlockFoods.servingsOf(block);
        }
        return Optional.empty();
    }

    public static Optional<EdibleBlock> resolve(BlockState state) {
        Block block = state.getBlock();

        if (block instanceof CakeBlock || block instanceof CandleCakeBlock) {
            return Optional.of(new EdibleBlock(cakeStack(block), 1.0F / (CakeBlock.MAX_BITES + 1)));
        }
        if (FarmersDelightCompat.isLoaded()) {
            Optional<EdibleBlock> farmersDelight = FarmersDelightEdibleBlockFoods.resolve(block);
            if (farmersDelight.isPresent()) {
                return farmersDelight;
            }
        }

        ItemStack stack = block.asItem().getDefaultInstance();
        if (!stack.has(DataComponents.FOOD)) {
            return Optional.empty();
        }
        return findBiteCount(state)
                .map(bites -> new EdibleBlock(stack, 1.0F / bites));
    }

    private static ItemStack cakeStack(Block block) {
        ItemStack stack = block.asItem().getDefaultInstance();
        return stack.isEmpty() ? Items.CAKE.getDefaultInstance() : stack;
    }

    private static Optional<Integer> findBiteCount(BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (!(property instanceof IntegerProperty integerProperty)) {
                continue;
            }
            for (String name : BITE_PROPERTY_NAMES) {
                if (integerProperty.getName().equals(name)) {
                    return Optional.of(Math.max(1, integerProperty.getPossibleValues().size()));
                }
            }
        }
        return Optional.empty();
    }

    public record Servings(ItemStack serving, float scale, int count, boolean sliced) {
        public static Servings slices(ItemStack slice, float scale, int count) {
            return new Servings(slice, scale, count, true);
        }

        public static Servings portions(ItemStack portion, int count) {
            return new Servings(portion, 1.0F, count, false);
        }
    }

    public record EdibleBlock(ItemStack stack, float biteScale) {
        public EdibleBlock {
            biteScale = Math.max(0.0F, biteScale);
        }
    }
}
