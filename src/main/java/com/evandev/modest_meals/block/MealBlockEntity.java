package com.evandev.modest_meals.block;

import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
//? if >=26.1 {
/*import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
*///?}

public class MealBlockEntity extends BlockEntity {
    private static final String CONTENTS_KEY = "contents";

    private MealContents contents = MealContents.EMPTY;

    public MealBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.MEAL_ENTITY.get(), pos, state);
    }

    public MealContents contents() {
        return contents;
    }

    //? if >=26.1 {
    /*@Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(CONTENTS_KEY, MealContents.CODEC, contents);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        contents = input.read(CONTENTS_KEY, MealContents.CODEC).orElse(MealContents.EMPTY);
        //? if fabric
        redraw();
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        MealContents fromItem = components.get(ModDataComponents.MEAL_CONTENTS.get());
        contents = fromItem == null ? MealContents.EMPTY : fromItem;
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard(CONTENTS_KEY);
    }
    *///?} else {
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        MealContents.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), contents)
                .result().ifPresent(encoded -> tag.put(CONTENTS_KEY, encoded));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        contents = tag.contains(CONTENTS_KEY)
                ? MealContents.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get(CONTENTS_KEY))
                        .result().orElse(MealContents.EMPTY)
                : MealContents.EMPTY;
        //? if fabric
        redraw();
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        MealContents fromItem = input.get(ModDataComponents.MEAL_CONTENTS.get());
        contents = fromItem == null ? MealContents.EMPTY : fromItem;
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove(CONTENTS_KEY);
    }
    //?}

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (contents != MealContents.EMPTY) {
            components.set(ModDataComponents.MEAL_CONTENTS.get(), contents);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    //? if neoforge && >=26.1 {
    /*@Override
    public void onDataPacket(Connection connection, ValueInput input) {
        super.onDataPacket(connection, input);
        redraw();
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        super.handleUpdateTag(input);
        redraw();
    }
    *///?} else if neoforge {
    /*@Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
        redraw();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        redraw();
    }
    *///?}

    private void redraw() {
        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }
}
