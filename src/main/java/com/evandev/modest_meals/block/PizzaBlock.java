package com.evandev.modest_meals.block;

import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.food.EdibleBlockContext;
import com.evandev.modest_meals.food.EdibleBlockFoods;
import com.evandev.modest_meals.food.FoodConsumption;
import com.evandev.modest_meals.registry.ModItems;
import com.evandev.modest_meals.tag.ModItemTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PizzaBlock extends BaseEntityBlock {
    public static final MapCodec<PizzaBlock> CODEC = simpleCodec(PizzaBlock::new);
    public static final int SLICES = 4;
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, SLICES - 1);

    /**
     * The quarter each slice occupies, in the order they are taken: north-east, south-east, south-west, north-west.
     */
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
        registerDefaultState(stateDefinition.any().setValue(BITES, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(BITES)];
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PizzaBlockEntity(pos, state);
    }

    public static MealContents contentsAt(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof PizzaBlockEntity pizza ? pizza.contents() : MealContents.EMPTY;
    }

    public static ItemStack sliceOf(MealContents contents) {
        ItemStack slice = new ItemStack(ModItems.PIZZA_SLICE.get());
        if (contents != MealContents.EMPTY) {
            slice.set(ModDataComponents.MEAL_CONTENTS.get(), contents.split(SLICES));
        }
        return slice;
    }

    public static ItemStack wholeOf(MealContents contents) {
        ItemStack whole = new ItemStack(ModItems.PIZZA.get());
        if (contents != MealContents.EMPTY) {
            whole.set(ModDataComponents.MEAL_CONTENTS.get(), contents);
        }
        return whole;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!held.is(ModItemTags.KNIVES)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            Direction face = hit.getDirection() == Direction.DOWN ? Direction.UP : hit.getDirection();
            Block.popResourceFromFace(level, pos, face, sliceOf(contentsAt(level, pos)));
            level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F);
            takeSlice(level, pos, state, player);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack slice = sliceOf(contentsAt(level, pos));
        FoodProperties food = slice.getFoodProperties(player);
        if (food == null || !FoodConsumption.canConsume(player, slice)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            EdibleBlockContext.Context previous = EdibleBlockContext.set(player, state, new EdibleBlockFoods.EdibleBlock(slice, 1.0F));
            try {
                player.getFoodData().eat(food);
            } finally {
                EdibleBlockContext.restore(player, previous);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, slice);
            }
            level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8F, 0.8F + level.random.nextFloat() * 0.4F);
            takeSlice(level, pos, state, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void takeSlice(Level level, BlockPos pos, BlockState state, Player player) {
        int bites = state.getValue(BITES);
        if (bites + 1 >= SLICES) {
            level.removeBlock(pos, false);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        } else {
            level.setBlock(pos, state.setValue(BITES, bites + 1), Block.UPDATE_ALL);
            level.gameEvent(player, GameEvent.EAT, pos);
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        MealContents contents = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof PizzaBlockEntity pizza
                ? pizza.contents()
                : MealContents.EMPTY;
        int bites = state.getValue(BITES);
        if (bites == 0) {
            return List.of(wholeOf(contents));
        }
        List<ItemStack> drops = new ArrayList<>();
        for (int i = bites; i < SLICES; i++) {
            drops.add(sliceOf(contents));
        }
        return drops;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return wholeOf(contentsAt(level, pos));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return (SLICES - state.getValue(BITES)) * 3;
    }
}
