package com.evandev.modest_meals.client;

import com.evandev.modest_meals.food.EdibleBlockFoods;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;

public class HoveredEdibleBlock {
    private static BlockState cachedState = null;
    private static Optional<EdibleBlockFoods.EdibleBlock> cachedFood = Optional.empty();

    public static Optional<EdibleBlockFoods.EdibleBlock> get(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        if (player != minecraft.player) {
            return Optional.empty();
        }

        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHit) || hitResult.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }
        if (minecraft.level == null) {
            return Optional.empty();
        }

        BlockState state = minecraft.level.getBlockState(blockHit.getBlockPos());
        if (state != cachedState) {
            cachedState = state;
            cachedFood = EdibleBlockFoods.resolve(state);
        }
        return cachedFood;
    }
}
