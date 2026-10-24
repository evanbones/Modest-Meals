package com.evandev.modest_meals.block;

import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds what a placed pizza was made from, so each slice keeps its share of the meal and the toppings can be drawn.
 */
public class PizzaBlockEntity extends BlockEntity {
    private static final String CONTENTS_KEY = "contents";

    private MealContents contents = MealContents.EMPTY;

    public PizzaBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.PIZZA_ENTITY.get(), pos, state);
    }

    public MealContents contents() {
        return contents;
    }

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
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        MealContents fromItem = input.get(ModDataComponents.MEAL_CONTENTS.get());
        contents = fromItem == null ? MealContents.EMPTY : fromItem;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (contents != MealContents.EMPTY) {
            components.set(ModDataComponents.MEAL_CONTENTS.get(), contents);
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove(CONTENTS_KEY);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
