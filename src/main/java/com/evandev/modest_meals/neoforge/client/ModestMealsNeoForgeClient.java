package com.evandev.modest_meals.neoforge.client;

//? if neoforge {
import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.client.ClientConfigScreen;
import com.evandev.modest_meals.client.HudLayers;
import com.evandev.modest_meals.client.MealBlockColors;
import com.evandev.modest_meals.client.ModestMealsClient;
import com.evandev.modest_meals.client.StaminaTextRenderer;
//? if <26.1
import com.evandev.modest_meals.client.model.DynamicMealBakedModel;
import com.evandev.modest_meals.client.model.PizzaRenderer;
import com.evandev.modest_meals.client.tooltip.FoodItemTooltips;
import com.evandev.modest_meals.compat.raised.RaisedCompat;
//? if neoforge_legacy
import com.evandev.modest_meals.compat.raised.RaisedLayerSync;
import com.evandev.modest_meals.registry.ModBlocks;
import com.evandev.modest_meals.registry.ModItems;
import net.minecraft.client.Minecraft;
//? if <26.1 {
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
//?} else {
/*import com.evandev.modest_meals.client.model.MealItemModel;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import java.util.List;
*///?}
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;


public final class ModestMealsNeoForgeClient {

    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> ClientConfigScreen.create(parent));

        modEventBus.addListener((RegisterKeyMappingsEvent event) -> event.register(ModestMealsClient.OPEN_FOOD_EDITOR_KEY));
        modEventBus.addListener(ModestMealsNeoForgeClient::registerGuiLayers);
        modEventBus.addListener(ModestMealsNeoForgeClient::addPackFinders);
        //? if >=26.1 {
        /*modEventBus.addListener((RegisterColorHandlersEvent.BlockTintSources event) ->
                event.register(List.of(MealBlockColors.TINT), ModBlocks.PIE.get(), ModBlocks.CAKE.get()));
        modEventBus.addListener((AddClientReloadListenersEvent event) -> event.addListener(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "hud_textures"), ModestMealsClient.RESOURCE_RELOAD_LISTENER));
        modEventBus.addListener((RegisterItemModelsEvent event) -> event.register(MealItemModel.ID, MealItemModel.Unbaked.MAP_CODEC));
        *///?} else {
        modEventBus.addListener((RegisterColorHandlersEvent.Block event) ->
                event.register(MealBlockColors::color, ModBlocks.PIE.get(), ModBlocks.CAKE.get()));
        modEventBus.addListener((RegisterClientReloadListenersEvent event) ->
                event.registerReloadListener(ModestMealsClient.RESOURCE_RELOAD_LISTENER));
        modEventBus.addListener(ModestMealsNeoForgeClient::modifyBakingResult);
        //?}
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerBlockEntityRenderer(ModBlocks.MEAL_ENTITY.get(), PizzaRenderer::new));

        IEventBus gameBus = NeoForge.EVENT_BUS;
        gameBus.addListener(EventPriority.HIGHEST, ModestMealsNeoForgeClient::hideVanillaLayers);
        gameBus.addListener(EventPriority.LOWEST, (ItemTooltipEvent event) ->
                FoodItemTooltips.onTooltip(event.getItemStack(), event.getToolTip(), event.getContext().tickRate()));
        gameBus.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> ModestMealsClient.onLoggingOut());
        gameBus.addListener((EntityLeaveLevelEvent event) -> {
            if (event.getLevel().isClientSide() && event.getEntity() instanceof Player player) {
                ModestMealsClient.onEntityLeaveClientLevel(player);
            }
        });
        gameBus.addListener((ClientTickEvent.Post event) -> ModestMealsClient.onClientTick());
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, ModestMealsClient.AIR_LAYER,
                (graphics, deltaTracker) -> HudLayers.renderClassicAir(graphics));
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, ModestMealsClient.ARMOR_LAYER,
                (graphics, deltaTracker) -> minecraft.gui.rightHeight = HudLayers.renderClassicArmor(graphics, minecraft.gui.rightHeight));
        event.registerAbove(ModestMealsClient.ARMOR_LAYER, ModestMealsClient.STAMINA_ARMOR_LAYER,
                (graphics, deltaTracker) -> minecraft.gui.rightHeight = HudLayers.renderClassicStamina(graphics, minecraft.gui.rightHeight));
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, ModestMealsClient.STAMINA_FOOD_LAYER,
                (graphics, deltaTracker) -> minecraft.gui.rightHeight = HudLayers.renderDefaultStamina(graphics, minecraft.gui.rightHeight));
        event.registerAbove(VanillaGuiLayers.CHAT, ModestMealsClient.STAMINA_TEXT_LAYER, StaminaTextRenderer::render);

        //? if neoforge_legacy {
        if (RaisedCompat.isLoaded()) {
            RaisedLayerSync.registerAndLinkToHotbar(ModestMealsClient.AIR_LAYER);
            RaisedLayerSync.registerAndLinkToHotbar(ModestMealsClient.ARMOR_LAYER);
            RaisedLayerSync.registerAndLinkToHotbar(ModestMealsClient.STAMINA_ARMOR_LAYER);
            RaisedLayerSync.registerAndLinkToHotbar(ModestMealsClient.STAMINA_FOOD_LAYER);
        }
        //?}
    }

    private static void hideVanillaLayers(RenderGuiLayerEvent.Pre event) {
        ResourceLocation layer = event.getName();
        if (layer.equals(VanillaGuiLayers.FOOD_LEVEL) && HudLayers.hidesVanillaFood()) {
            event.setCanceled(true);
        } else if ((layer.equals(VanillaGuiLayers.ARMOR_LEVEL) || layer.equals(VanillaGuiLayers.AIR_LEVEL))
                && HudLayers.hidesVanillaArmorAndAir()) {
            event.setCanceled(true);
        }
    }

    private static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            event.addPackFinders(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "resourcepacks/" + ModestMealsClient.BAISYLIA_HUD_PACK),
                    PackType.CLIENT_RESOURCES, ModestMealsClient.BAISYLIA_HUD_TITLE, PackSource.BUILT_IN, false, Pack.Position.TOP);
        }
    }

    //? if <26.1 {
    private static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
        for (var meal : ModItems.MEALS) {
            ModelResourceLocation modelLoc = ModelResourceLocation.inventory(BuiltInRegistries.ITEM.getKey(meal.get()));
            BakedModel original = event.getModels().get(modelLoc);
            if (original != null) {
                event.getModels().put(modelLoc, DynamicMealBakedModel.wrap(meal, original));
            }
        }
    }
    //?}
}
//?}
