package com.evandev.modest_meals.fabric.client;

//? if fabric && >=26.1 {
/*import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.client.HudLayers;
import com.evandev.modest_meals.client.RenderCompat;
import com.evandev.modest_meals.client.StaminaTextRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.ResourceLocation;

public final class FabricHud {
    private static final int FOOD_ROW_HEIGHT = 39;
    private static final int AIR_ROW_HEIGHT = 49;

    private static int airShift;

    public static void register() {
        HudElementRegistry.replaceElement(VanillaHudElements.ARMOR_BAR, original -> (graphics, deltaTracker) -> {
            if (!HudLayers.hidesVanillaArmorAndAir()) {
                original.extractRenderState(graphics, deltaTracker);
            }
        });
        HudElementRegistry.replaceElement(VanillaHudElements.FOOD_BAR, original -> (graphics, deltaTracker) -> {
            int rightHeight = FOOD_ROW_HEIGHT;
            if (!HudLayers.hidesVanillaFood()) {
                original.extractRenderState(graphics, deltaTracker);
                rightHeight = AIR_ROW_HEIGHT;
            }
            rightHeight = HudLayers.renderClassicArmor(graphics, rightHeight);
            rightHeight = HudLayers.renderClassicStamina(graphics, rightHeight);
            rightHeight = HudLayers.renderDefaultStamina(graphics, rightHeight);
            airShift = rightHeight - AIR_ROW_HEIGHT;
        });
        HudElementRegistry.replaceElement(VanillaHudElements.AIR_BAR, original -> (graphics, deltaTracker) -> {
            if (HudLayers.hidesVanillaArmorAndAir()) {
                HudLayers.renderClassicAir(graphics);
                return;
            }
            RenderCompat.pushPose(graphics);
            RenderCompat.translate(graphics, 0.0F, -airShift);
            original.extractRenderState(graphics, deltaTracker);
            RenderCompat.popPose(graphics);
        });
        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT,
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "stamina_text"), StaminaTextRenderer::render);
    }
}
*///?}
