package com.evandev.modest_meals.block;

import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.food.EdibleBlockContext;
import com.evandev.modest_meals.food.EdibleBlockFoods;
import com.evandev.modest_meals.food.FoodConsumption;
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
//? if >=26.1 {
/*import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
*///?} else
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

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
public abstract class SlicedMealBlock extends BaseEntityBlock {

    protected SlicedMealBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(bites(), 0));
    }

    public abstract IntegerProperty bites();

    public abstract int slices();

    protected abstract VoxelShape shape(int bites);

    protected abstract Item wholeItem();

    protected abstract Item sliceItem();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(bites());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state.getValue(bites()));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MealBlockEntity(pos, state);
    }

    public static MealContents contentsAt(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MealBlockEntity meal ? meal.contents() : MealContents.EMPTY;
    }

    public ItemStack sliceOf(MealContents contents) {
        ItemStack slice = new ItemStack(sliceItem());
        if (contents != MealContents.EMPTY) {
            slice.set(ModDataComponents.MEAL_CONTENTS.get(), contents.split(slices()));
        }
        return slice;
    }

    public ItemStack wholeOf(MealContents contents) {
        ItemStack whole = new ItemStack(wholeItem());
        if (contents != MealContents.EMPTY) {
            whole.set(ModDataComponents.MEAL_CONTENTS.get(), contents);
        }
        return whole;
    }

    @Override
    //? if >=26.1 {
    /*protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        if (!held.is(ModItemTags.KNIVES)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
    *///?} else {
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!held.is(ModItemTags.KNIVES)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    //?}
        if (!level.isClientSide()) {
            Direction face = hit.getDirection() == Direction.DOWN ? Direction.UP : hit.getDirection();
            Block.popResourceFromFace(level, pos, face, sliceOf(contentsAt(level, pos)));
            level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F);
            takeSlice(level, pos, state, player);
        }
        //? if >=26.1 {
        /*return InteractionResult.SUCCESS;
        *///?} else
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack slice = sliceOf(contentsAt(level, pos));
        FoodProperties food = FoodConsumption.foodOf(slice, player);
        if (food == null || !FoodConsumption.canConsume(player, slice)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            EdibleBlockContext.Context previous = EdibleBlockContext.set(player, state, new EdibleBlockFoods.EdibleBlock(slice, 1.0F));
            try {
                player.getFoodData().eat(food);
            } finally {
                EdibleBlockContext.restore(player, previous);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, slice);
            }
            //? if >=26.1 {
            /*level.playSound(null, pos, SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 0.8F, 0.8F + level.getRandom().nextFloat() * 0.4F);
            *///?} else
            level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8F, 0.8F + level.getRandom().nextFloat() * 0.4F);
            takeSlice(level, pos, state, player);
        }
        //? if >=26.1 {
        /*return InteractionResult.SUCCESS;
        *///?} else
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void takeSlice(Level level, BlockPos pos, BlockState state, Player player) {
        int bites = state.getValue(bites());
        if (bites + 1 >= slices()) {
            level.removeBlock(pos, false);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        } else {
            level.setBlock(pos, state.setValue(bites(), bites + 1), Block.UPDATE_ALL);
            level.gameEvent(player, GameEvent.EAT, pos);
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        MealContents contents = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof MealBlockEntity meal
                ? meal.contents()
                : MealContents.EMPTY;
        int bites = state.getValue(bites());
        if (bites == 0) {
            return List.of(wholeOf(contents));
        }
        List<ItemStack> drops = new ArrayList<>();
        for (int i = bites; i < slices(); i++) {
            drops.add(sliceOf(contents));
        }
        return drops;
    }

    //? if >=26.1 {
    /*@Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return wholeOf(contentsAt(level, pos));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }
    *///?} else {
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
    //?}

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    //? if >=26.1 {
    /*protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
    *///?} else
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return Math.round((slices() - state.getValue(bites())) * 15.0F / slices());
    }
}
