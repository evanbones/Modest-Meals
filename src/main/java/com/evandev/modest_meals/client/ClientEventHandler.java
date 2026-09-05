package com.evandev.modest_meals.client;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.client.gui.FoodEditorScreen;
import com.evandev.modest_meals.client.hud.TextureHelper;
import com.evandev.modest_meals.client.tooltip.FoodItemTooltips;
import com.evandev.modest_meals.compat.raised.RaisedCompat;
import com.evandev.modest_meals.compat.raised.RaisedLayerSync;
import com.evandev.modest_meals.config.HudLayoutOption;
import com.evandev.modest_meals.config.ModConfig;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.nio.file.Path;
import java.util.Optional;

public class ClientEventHandler {

    public static final ResourceLocation AIR_LAYER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "air");
    public static final ResourceLocation ARMOR_LAYER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "armor");
    public static final ResourceLocation STAMINA_ARMOR_LAYER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "stamina_armor");
    public static final ResourceLocation STAMINA_FOOD_LAYER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "stamina_food");
    public static final ResourceLocation STAMINA_TEXT_LAYER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "stamina_text");

    public static final KeyMapping OPEN_FOOD_EDITOR_KEY = new KeyMapping(
            "key.modest_meals.open_food_editor",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "key.categories.modest_meals"
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_FOOD_EDITOR_KEY);
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.PLAYER_HEALTH,
                AIR_LAYER,
                (graphics, deltaTracker) -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    if (ModConfig.get().hudLayout != HudLayoutOption.CLASSIC || !ModConfig.get().hideHungerBar || minecraft.options.hideGui || (minecraft.gameMode != null && !minecraft.gameMode.canHurtPlayer())) {
                        return;
                    }

                    Player player = minecraft.player;
                    if (player == null) {
                        return;
                    }

                    int supply = player.getAirSupply();
                    int maximum = player.getMaxAirSupply();
                    if (supply >= maximum) {
                        return;
                    }

                    int offsetLeft = graphics.guiWidth() / 2 - 100;

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableDepthTest();

                    HudHelper.renderAir(graphics, HudHelper.getHeightOffsetFromHearts(), offsetLeft);

                    RenderSystem.disableBlend();
                }
        );

        if (RaisedCompat.isLoaded()) {
            RaisedLayerSync.linkToHotbarIfUnconfigured(AIR_LAYER);
        }

        event.registerAbove(
                VanillaGuiLayers.EXPERIENCE_BAR,
                ARMOR_LAYER,
                (graphics, deltaTracker) -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    if (ModConfig.get().hudLayout != HudLayoutOption.CLASSIC || !ModConfig.get().hideHungerBar || minecraft.options.hideGui || (minecraft.gameMode != null && !minecraft.gameMode.canHurtPlayer())) {
                        return;
                    }

                    int offsetRight = graphics.guiWidth() / 2 + 90;

                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableDepthTest();

                    HudHelper.renderArmor(graphics, minecraft.gui.rightHeight, offsetRight);

                    if (!HudHelper.isArmorEmpty()) {
                        minecraft.gui.rightHeight += 10;
                    }

                    RenderSystem.disableBlend();
                }
        );

        if (RaisedCompat.isLoaded()) {
            RaisedLayerSync.linkToHotbarIfUnconfigured(ARMOR_LAYER);
        }

        event.registerAbove(
                ARMOR_LAYER,
                STAMINA_ARMOR_LAYER,
                (graphics, deltaTracker) -> {
                    if (!StaminaRenderer.isVisible() || ModConfig.get().hudLayout != HudLayoutOption.CLASSIC || !ModConfig.get().hideHungerBar) {
                        return;
                    }

                    Minecraft minecraft = Minecraft.getInstance();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableDepthTest();

                    StaminaRenderer.render(graphics, minecraft.gui.rightHeight, 0);
                    minecraft.gui.rightHeight += StaminaRenderer.occupiedHeight();

                    RenderSystem.disableBlend();
                }
        );

        if (RaisedCompat.isLoaded()) {
            RaisedLayerSync.linkToHotbarIfUnconfigured(STAMINA_ARMOR_LAYER);
        }

        event.registerAbove(
                VanillaGuiLayers.FOOD_LEVEL,
                STAMINA_FOOD_LAYER,
                (graphics, deltaTracker) -> {
                    if (!StaminaRenderer.isVisible() || (ModConfig.get().hudLayout == HudLayoutOption.CLASSIC && ModConfig.get().hideHungerBar)) {
                        return;
                    }

                    Minecraft minecraft = Minecraft.getInstance();
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.disableDepthTest();

                    StaminaRenderer.render(graphics, minecraft.gui.rightHeight, 0);
                    minecraft.gui.rightHeight += StaminaRenderer.occupiedHeight();

                    RenderSystem.disableBlend();
                }
        );

        if (RaisedCompat.isLoaded()) {
            RaisedLayerSync.linkToHotbarIfUnconfigured(STAMINA_FOOD_LAYER);
        }

        event.registerAbove(
                VanillaGuiLayers.CHAT,
                STAMINA_TEXT_LAYER,
                StaminaTextRenderer::render
        );
    }

    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            Path resourcePath = ModList.get().getModFileById(Constants.MOD_ID).getFile().findResource("resourcepacks/baisylia_hud");
            PackLocationInfo info = new PackLocationInfo(
                    Constants.MOD_ID + "_baisylia_hud",
                    Component.translatable("pack.modest_meals.baisylia_hud.title"),
                    PackSource.BUILT_IN,
                    Optional.empty()
            );

            Pack.ResourcesSupplier supplier = new PathPackResources.PathResourcesSupplier(resourcePath);
            PackSelectionConfig selectionConfig = new PackSelectionConfig(false, Pack.Position.TOP, false);
            Pack pack = Pack.readMetaAndCreate(info, supplier, PackType.CLIENT_RESOURCES, selectionConfig);

            if (pack != null) {
                event.addRepositorySource(packConsumer -> packConsumer.accept(pack));
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            try {
                new TextureHelper().generateHudTextures();
            } catch (Exception ignored) {
            }
        });
    }

    public static class GameEvents {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
            ResourceLocation layer = event.getName();
            boolean isFoodOff = ModConfig.get().hideHungerBar;
            HudLayoutOption layout = ModConfig.get().hudLayout;

            if (isFoodOff) {
                if (layer.equals(VanillaGuiLayers.FOOD_LEVEL)) {
                    event.setCanceled(true);
                } else if (layout == HudLayoutOption.CLASSIC && (layer.equals(VanillaGuiLayers.ARMOR_LEVEL) || layer.equals(VanillaGuiLayers.AIR_LEVEL))) {
                    event.setCanceled(true);
                }
            }
        }

        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            FoodItemTooltips.onTooltip(event);
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            while (OPEN_FOOD_EDITOR_KEY.consumeClick()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.screen == null) {
                    minecraft.setScreen(new FoodEditorScreen(null));
                }
            }
        }
    }
}
