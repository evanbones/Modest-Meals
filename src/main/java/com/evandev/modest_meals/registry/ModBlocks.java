package com.evandev.modest_meals.registry;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.platform.Platform;
import com.evandev.modest_meals.platform.RegistrationProvider;
import com.evandev.modest_meals.block.PizzaBlock;
import com.evandev.modest_meals.block.CakeBlock;
import com.evandev.modest_meals.block.MealBlockEntity;
import com.evandev.modest_meals.block.PieBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

public class ModBlocks {
    private static final RegistrationProvider<Block> BLOCKS = RegistrationProvider.get(Registries.BLOCK);
    private static final RegistrationProvider<BlockEntityType<?>> BLOCK_ENTITIES =
            RegistrationProvider.get(Registries.BLOCK_ENTITY_TYPE);

    public static final Supplier<PizzaBlock> PIZZA = BLOCKS.register("pizza", () -> new PizzaBlock(
            properties("pizza")
                    .strength(0.5F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    public static final Supplier<PieBlock> PIE = BLOCKS.register("pie", () -> new PieBlock(
            properties("pie")
                    .strength(0.5F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    public static final Supplier<CakeBlock> CAKE = BLOCKS.register("cake", () -> new CakeBlock(
            properties("cake")
                    .strength(0.5F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    public static final Supplier<BlockEntityType<MealBlockEntity>> MEAL_ENTITY = BLOCK_ENTITIES.register("pizza",
            () -> Platform.blockEntityType(MealBlockEntity::new, PIZZA.get(), PIE.get(), CAKE.get()));

    private static BlockBehaviour.Properties properties(String name) {
        //? if >=26.1 {
        /*return BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name)));
        *///?} else
        return BlockBehaviour.Properties.of();
    }

    public static void init() {
    }
}
