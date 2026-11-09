package com.evandev.modest_meals.fabric.client;

//? if fabric {
import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.client.MealBlockColors;
import com.evandev.modest_meals.client.ModestMealsClient;
import com.evandev.modest_meals.client.StaminaTextRenderer;
import com.evandev.modest_meals.client.model.PizzaRenderer;
import com.evandev.modest_meals.client.tooltip.FoodItemTooltips;
import com.evandev.modest_meals.network.ClientPacketContext;
import com.evandev.modest_meals.network.ClientboundPayload;
import com.evandev.modest_meals.network.ModNetworking;
import com.evandev.modest_meals.registry.ModBlocks;
import com.evandev.modest_meals.registry.ModItems;
import net.fabricmc.api.ClientModInitializer;
//? if >=26.1 {
/*import com.evandev.modest_meals.client.model.MealItemModel;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.minecraft.client.renderer.item.ItemModels;
import java.util.List;
*///?} else {
import com.evandev.modest_meals.client.model.DynamicMealBakedModel;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import com.evandev.modest_meals.fabric.ModestMealsFabric;
//?}
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModestMealsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModNetworking.CLIENTBOUND.forEach(ModestMealsFabricClient::registerReceiver);

        BlockEntityRendererRegistry.register(ModBlocks.MEAL_ENTITY.get(), PizzaRenderer::new);
        ResourceLocation hudPackId = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, ModestMealsClient.BAISYLIA_HUD_PACK);
        ResourceLocation hudTexturesId = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "hud_textures");
        //? if >=26.1 {
        /*KeyMappingHelper.registerKeyMapping(ModestMealsClient.OPEN_FOOD_EDITOR_KEY);
        BlockColorRegistry.register(List.of(MealBlockColors.TINT), ModBlocks.PIE.get(), ModBlocks.CAKE.get());
        FabricLoader.getInstance().getModContainer(Constants.MOD_ID).ifPresent(container -> ResourceLoader.registerBuiltinPack(
                hudPackId, container, ModestMealsClient.BAISYLIA_HUD_TITLE, PackActivationType.NORMAL));
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(hudTexturesId, ModestMealsClient.RESOURCE_RELOAD_LISTENER);
        ItemModels.ID_MAPPER.put(MealItemModel.ID, MealItemModel.Unbaked.MAP_CODEC);
        FabricHud.register();
        *///?} else {
        KeyBindingHelper.registerKeyBinding(ModestMealsClient.OPEN_FOOD_EDITOR_KEY);
        ColorProviderRegistry.BLOCK.register(MealBlockColors::color, ModBlocks.PIE.get(), ModBlocks.CAKE.get());
        FabricLoader.getInstance().getModContainer(Constants.MOD_ID).ifPresent(container ->
                ResourceManagerHelper.registerBuiltinResourcePack(hudPackId, container, ModestMealsClient.BAISYLIA_HUD_TITLE,
                        ResourcePackActivationType.NORMAL));
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                ModestMealsFabric.identified(hudTexturesId, ModestMealsClient.RESOURCE_RELOAD_LISTENER));

        ModelLoadingPlugin.register(context -> {
            Map<ResourceLocation, Supplier<Item>> meals = mealsById();
            context.modifyModelAfterBake().register((model, modifierContext) -> {
                ModelResourceLocation id = modifierContext.topLevelId();
                if (model == null || id == null || !ModelResourceLocation.INVENTORY_VARIANT.equals(id.variant())) {
                    return model;
                }
                Supplier<Item> meal = meals.get(id.id());
                return meal == null ? model : DynamicMealBakedModel.wrap(meal, model);
            });
        });
        HudRenderCallback.EVENT.register(StaminaTextRenderer::render);
        //?}

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipFlag, lines) ->
                FoodItemTooltips.onTooltip(stack, lines, tooltipContext.tickRate()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ModestMealsClient.onLoggingOut());
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof Player player) {
                ModestMealsClient.onEntityLeaveClientLevel(player);
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> ModestMealsClient.onClientTick());
    }

    //? if <26.1 {
    private static Map<ResourceLocation, Supplier<Item>> mealsById() {
        Map<ResourceLocation, Supplier<Item>> meals = new HashMap<>();
        ModItems.MEALS.forEach(meal -> meals.put(BuiltInRegistries.ITEM.getKey(meal.get()), meal));
        return meals;
    }
    //?}

    private static <T extends CustomPacketPayload> void registerReceiver(ClientboundPayload<T> payload) {
        ClientPlayNetworking.registerGlobalReceiver(payload.type(), (message, context) ->
                payload.handler().accept(message, new ClientPacketContext(context.player(),
                        context.client().getConnection() != null && context.client().getConnection().getConnection().isMemoryConnection())));
    }
}
//?}
