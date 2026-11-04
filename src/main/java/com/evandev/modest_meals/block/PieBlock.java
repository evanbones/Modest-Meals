package com.evandev.modest_meals.block;

import com.evandev.modest_meals.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PieBlock extends SlicedMealBlock {
    public static final MapCodec<PieBlock> CODEC = simpleCodec(PieBlock::new);
    public static final int SLICES = 4;
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, SLICES - 1);

    private static final VoxelShape[] SHAPES = {
            Block.box(2, 0, 2, 14, 4, 14),
            Shapes.or(Block.box(2, 0, 8, 14, 4, 14), Block.box(8, 0, 2, 14, 4, 8)),
            Block.box(2, 0, 8, 14, 4, 14),
            Block.box(8, 0, 8, 14, 4, 14)
    };

    public PieBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

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
        return ModItems.PIE.get();
    }

    @Override
    protected Item sliceItem() {
        return ModItems.PIE_SLICE.get();
    }
}
