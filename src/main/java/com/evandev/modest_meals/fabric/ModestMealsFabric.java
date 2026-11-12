package com.evandev.modest_meals.fabric;

//? if fabric {
/*import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.ModestMeals;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.event.CommonEvents;
import com.evandev.modest_meals.network.ClientboundPayload;
import com.evandev.modest_meals.network.ModNetworking;
import com.evandev.modest_meals.platform.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
//? if >=26.1 {
/^import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
^///?} else {
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
//?}
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ModestMealsFabric implements ModInitializer {
    //? if >=26.1
    //private static final int MAX_PAYLOAD_SIZE = 16 * 1024 * 1024;

    @Override
    public void onInitialize() {
        ModestMeals.init();
        ModConfig.load();

        ModNetworking.CLIENTBOUND.forEach(ModestMealsFabric::registerPayloadType);

        ServerLifecycleEvents.SERVER_STARTING.register(Platform::setCurrentServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> Platform.setCurrentServer(null));
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> CommonEvents.onDataPackReloaded(server));
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> CommonEvents.onDatapackSync(List.of(player)));

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) ->
                CommonEvents.allowBlockInteraction(player, level, hitResult.getBlockPos()) ? InteractionResult.PASS : InteractionResult.FAIL);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> CommonEvents.onPlayerLoggedIn(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> CommonEvents.onPlayerLoggedOut(handler.player));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> CommonEvents.onPlayerClone(oldPlayer, newPlayer, !alive));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> CommonEvents.onPlayerRespawn(newPlayer));

        //? if >=26.1 {
        /^ResourceLoader serverData = ResourceLoader.get(PackType.SERVER_DATA);
        CommonEvents.serverReloadListeners().forEach((name, listener) ->
                serverData.registerReloadListener(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), listener));
        ^///?} else {
        ResourceManagerHelper serverData = ResourceManagerHelper.get(PackType.SERVER_DATA);
        CommonEvents.serverReloadListeners().forEach((name, listener) ->
                serverData.registerReloadListener(identified(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), listener)));
        //?}
    }

    private static <T extends CustomPacketPayload> void registerPayloadType(ClientboundPayload<T> payload) {
        //? if >=26.1 {
        /^PayloadTypeRegistry.clientboundPlay().registerLarge(payload.type(), payload.codec(), MAX_PAYLOAD_SIZE);
        ^///?} else
        PayloadTypeRegistry.playS2C().register(payload.type(), payload.codec());
    }

    //? if <26.1 {
    public static IdentifiableResourceReloadListener identified(ResourceLocation id, PreparableReloadListener listener) {
        return new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return id;
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager resourceManager,
                                                  ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                                  Executor backgroundExecutor, Executor gameExecutor) {
                return listener.reload(barrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            }

            @Override
            public String getName() {
                return listener.getName();
            }
        };
    }
    //?}
}
*///?}
