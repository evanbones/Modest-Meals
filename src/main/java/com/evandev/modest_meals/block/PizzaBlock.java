package com.evandev.modest_meals.block;

import com.evandev.modest_meals.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PizzaBlock extends SlicedMealBlock {
    //? if <26.3
    public static final MapCodec<PizzaBlock> CODEC = simpleCodec(PizzaBlock::new);
    public static final int SLICES = 4;
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, SLICES - 1);

    public static final int[][] QUADRANTS = {
            {8, 0, 16, 8},
            {8, 8, 16, 16},
            {0, 8, 8, 16},
            {0, 0, 8, 8}
    };
    private static final VoxelShape[] SHAPES = new VoxelShape[SLICES];

    static {
        for (int bites = 0; bites < SLICES; bites++) {
            VoxelShape shape = Shapes.empty();
            for (int quadrant = bites; quadrant < SLICES; quadrant++) {
                int[] q = QUADRANTS[quadrant];
                shape = Shapes.or(shape, Block.box(q[0], 0, q[1], q[2], 2, q[3]));
            }
            SHAPES[bites] = shape;
        }
    }

    public PizzaBlock(Properties properties) {
        super(properties);
    }

    //? if <26.3 {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    public IntegerProperty bites() {
        return BITES;
    }

    @Override
    public int slices() {
        return SLICES;
    }

    @Override
    protected VoxelShape shape(int bites) {
        return SHAPES[bites];
    }

    @Override
    protected Item wholeItem() {
        return ModItems.PIZZA.get();
    }

    @Override
    protected Item sliceItem() {
        return ModItems.PIZZA_SLICE.get();
    }
}
