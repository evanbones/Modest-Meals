package com.evandev.modest_meals.client;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.client.gui.FoodEditorScreen;
import com.evandev.modest_meals.client.hud.TextureHelper;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;

public final class ModestMealsClient {
    public static final ResourceLocation AIR_LAYER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "air");
    public static final ResourceLocation ARMOR_LAYER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "armor");
    public static final ResourceLocation STAMINA_ARMOR_LAYER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "stamina_armor");
    public static final ResourceLocation STAMINA_FOOD_LAYER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "stamina_food");
    public static final ResourceLocation STAMINA_TEXT_LAYER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "stamina_text");

    public static final String BAISYLIA_HUD_PACK = "baisylia_hud";
    public static final Component BAISYLIA_HUD_TITLE = Component.translatable("pack.modest_meals.baisylia_hud.title");

    public static final KeyMapping OPEN_FOOD_EDITOR_KEY = new KeyMapping(
            "key.modest_meals.open_food_editor",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            //? if >=26.1 {
            /*KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.MOD_ID))
            *///?} else
            "key.categories.modest_meals"
    );

    public static final ResourceManagerReloadListener RESOURCE_RELOAD_LISTENER = resourceManager -> {
        MealBlockColors.clearCache();
        try {
            new TextureHelper().generateHudTextures();
        } catch (Exception e) {
            Constants.LOG.warn("Failed to generate HUD textures", e);
        }
    };

    public static void onClientTick() {
        while (OPEN_FOOD_EDITOR_KEY.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen == null) {
                minecraft.setScreen(new FoodEditorScreen(null));
            }
        }
    }

    public static void onLoggingOut() {
        StaminaHelper.clearClient();
        ModConfig.applyServerValues(null);
    }

    public static void onEntityLeaveClientLevel(Player player) {
        StaminaHelper.forget(player);
    }
}
