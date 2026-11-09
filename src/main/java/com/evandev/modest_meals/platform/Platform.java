package com.evandev.modest_meals.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

//? if neoforge {
/*import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
*///?} else {
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
//?}

public class Platform {
    //? if fabric
    private static MinecraftServer currentServer;

    public static boolean isModLoaded(String modId) {
        //? if neoforge {
        /*ModList modList = ModList.get();
        return modList != null && modList.isLoaded(modId);
        *///?} else
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static boolean isPhysicalClient() {
        //? if neoforge {
        /*return FMLEnvironment.dist.isClient();
        *///?} else
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static Path getConfigDirectory() {
        //? if neoforge {
        /*return FMLPaths.CONFIGDIR.get();
        *///?} else
        return FabricLoader.getInstance().getConfigDir();
    }

    public static @Nullable MinecraftServer getCurrentServer() {
        //? if neoforge {
        /*return ServerLifecycleHooks.getCurrentServer();
        *///?} else
        return currentServer;
    }

    //? if fabric {
    public static void setCurrentServer(@Nullable MinecraftServer server) {
        currentServer = server;
    }
    //?}

    public static <T extends BlockEntity> BlockEntityType<T> blockEntityType(BlockEntityType.BlockEntitySupplier<T> factory, Block... blocks) {
        //? if >=26.1 && neoforge {
        /*return new BlockEntityType<>(factory, java.util.Set.of(blocks));
        *///?} else if >=26.1 {
        /*return net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(factory::create, blocks).build();
        *///?} else
        return BlockEntityType.Builder.of(factory, blocks).build(null);
    }

    public static boolean isFakePlayer(Player player) {
        return player instanceof FakePlayer;
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        //? if neoforge {
        /*PacketDistributor.sendToPlayer(player, payload);
        *///?} else
        ServerPlayNetworking.send(player, payload);
    }
}
