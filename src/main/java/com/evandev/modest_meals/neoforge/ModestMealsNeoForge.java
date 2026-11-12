package com.evandev.modest_meals.neoforge;

//? if neoforge {
import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.ModestMeals;
import com.evandev.modest_meals.attribute.ModAttributes;
//? if neoforge_legacy
import com.evandev.modest_meals.compat.cold_sweat.ColdSweatCompat;
//? if shield_expansion
import com.evandev.modest_meals.compat.shield_expansion.ShieldExpansionCompat;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.event.CommonEvents;
import com.evandev.modest_meals.food.CustomFoodDatapack;
import com.evandev.modest_meals.network.ClientPacketContext;
import com.evandev.modest_meals.network.ClientboundPayload;
import com.evandev.modest_meals.network.ModNetworking;
import com.evandev.modest_meals.neoforge.client.ModestMealsNeoForgeClient;
import com.evandev.modest_meals.platform.RegistrationProvider;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
//? if >=26.1 {
/*import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
*///?} else
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Constants.MOD_ID)
public class ModestMealsNeoForge {
    public ModestMealsNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        ModestMeals.init();
        RegistrationProvider.registerAll(modEventBus);

        modEventBus.addListener((FMLCommonSetupEvent event) -> ModConfig.load());
        modEventBus.addListener(ModestMealsNeoForge::registerPayloads);
        modEventBus.addListener(ModestMealsNeoForge::addPackFinders);
        modEventBus.addListener((EntityAttributeModificationEvent event) ->
                event.add(EntityType.PLAYER, ModAttributes.STAMINA_CAPACITY));

        IEventBus gameBus = NeoForge.EVENT_BUS;
        gameBus.addListener((PlayerTickEvent.Post event) -> CommonEvents.onPlayerTick(event.getEntity()));
        gameBus.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            if (!CommonEvents.allowBlockInteraction(event.getEntity(), event.getLevel(), event.getPos())) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
        });
        gameBus.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                CommonEvents.onPlayerLoggedIn(player);
            }
        });
        gameBus.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> CommonEvents.onPlayerLoggedOut(event.getEntity()));
        gameBus.addListener((PlayerEvent.Clone event) ->
                CommonEvents.onPlayerClone(event.getOriginal(), event.getEntity(), event.isWasDeath()));
        gameBus.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                CommonEvents.onPlayerRespawn(player);
            }
        });
        //? if >=26.1 {
        /*gameBus.addListener((AddServerReloadListenersEvent event) -> CommonEvents.serverReloadListeners().forEach((name, listener) ->
                event.addListener(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), listener)));
        *///?} else {
        gameBus.addListener((AddReloadListenerEvent event) ->
                CommonEvents.serverReloadListeners().values().forEach(event::addListener));
        //?}
        gameBus.addListener((OnDatapackSyncEvent event) -> {
            if (event.getPlayer() == null) {
                CommonEvents.onDataPackReloaded(event.getPlayerList().getServer());
            }
            CommonEvents.onDatapackSync(event.getRelevantPlayers().toList());
        });
        gameBus.addListener((LivingEntityUseItemEvent.Finish event) ->
                CommonEvents.onItemConsumed(event.getEntity(), event.getItem()));

        //? if neoforge_legacy
        ColdSweatCompat.register();
        //? if shield_expansion
        ShieldExpansionCompat.init();

        if (FMLEnvironment.dist.isClient()) {
            ModestMealsNeoForgeClient.init(modEventBus, modContainer);
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ModNetworking.PROTOCOL_VERSION);
        ModNetworking.CLIENTBOUND.forEach(payload -> registerClientbound(registrar, payload));
    }

    private static <T extends CustomPacketPayload> void registerClientbound(PayloadRegistrar registrar, ClientboundPayload<T> payload) {
        registrar.playToClient(payload.type(), payload.codec(), (message, context) -> context.enqueueWork(() ->
                payload.handler().accept(message, new ClientPacketContext(context.player(), context.connection().isMemoryConnection()))));
    }

    private static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            CustomFoodDatapack.createPack().ifPresent(pack -> event.addRepositorySource(consumer -> consumer.accept(pack)));
        }
    }
}
//?}
