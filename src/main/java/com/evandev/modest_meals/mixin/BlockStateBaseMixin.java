package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.food.EdibleBlockContext;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Shadow
    protected abstract BlockState asState();

    @WrapMethod(method = "useItemOn")
    private ItemInteractionResult mm$scopeItemInteraction(
            ItemStack stack, Level level, Player player, InteractionHand hand, BlockHitResult hitResult,
            Operation<ItemInteractionResult> original
    ) {
        return mm$withBlockContext(player, () -> original.call(stack, level, player, hand, hitResult));
    }

    @WrapMethod(method = "useWithoutItem")
    private InteractionResult mm$scopeEmptyHandInteraction(
            Level level, Player player, BlockHitResult hitResult, Operation<InteractionResult> original
    ) {
        return mm$withBlockContext(player, () -> original.call(level, player, hitResult));
    }

    @Unique
    private <T> T mm$withBlockContext(Player player, Supplier<T> interaction) {
        EdibleBlockContext.Context previous = EdibleBlockContext.set(player, this.asState());
        try {
            return interaction.get();
        } finally {
            EdibleBlockContext.restore(player, previous);
        }
    }
}
