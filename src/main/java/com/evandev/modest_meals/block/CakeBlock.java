package com.evandev.modest_meals.block;

import com.evandev.modest_meals.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CakeBlock extends SlicedMealBlock {
    //? if <26.3
    public static final MapCodec<CakeBlock> CODEC = simpleCodec(CakeBlock::new);
    public static final int SLICES = 7;
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, SLICES - 1);

    public CakeBlock(Properties properties) {
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
        return Block.box(1 + bites * 2, 0, 1, 15, 8, 15);
    }

    @Override
    protected Item wholeItem() {
        return ModItems.CAKE.get();
    }

    @Override
    protected Item sliceItem() {
        return ModItems.CAKE_SLICE.get();
    }
}
