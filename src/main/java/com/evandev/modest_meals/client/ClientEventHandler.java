package com.evandev.modest_meals.client;

import com.evandev.modest_meals.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Path;
import java.util.Optional;

public class ClientEventHandler {

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.FOOD_LEVEL,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "stamina_bar"),
                StaminaRenderer::render
        );

        event.registerAbove(
                VanillaGuiLayers.CHAT,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "stamina_text"),
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
}
