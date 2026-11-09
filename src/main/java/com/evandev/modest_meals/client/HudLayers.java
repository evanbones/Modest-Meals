package com.evandev.modest_meals.client;

import com.evandev.modest_meals.config.HudLayoutOption;
import com.evandev.modest_meals.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public final class HudLayers {

    private static boolean isClassicWithoutHunger() {
        return ModConfig.get().hudLayout == HudLayoutOption.CLASSIC && ModConfig.get().hideHungerBar;
    }

    public static boolean hidesVanillaFood() {
        return ModConfig.get().hideHungerBar;
    }

    public static boolean hidesVanillaArmorAndAir() {
        return isClassicWithoutHunger();
    }

    private static boolean isSurvivalHudHidden(Minecraft minecraft) {
        return minecraft.options.hideGui || (minecraft.gameMode != null && !minecraft.gameMode.canHurtPlayer());
    }

    public static void renderClassicAir(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isClassicWithoutHunger() || isSurvivalHudHidden(minecraft)) {
            return;
        }
        Player player = minecraft.player;
        if (player == null || player.getAirSupply() >= player.getMaxAirSupply()) {
            return;
        }
        prepareBlend();
        HudHelper.renderAir(graphics, HudHelper.getHeightOffsetFromHearts(), graphics.guiWidth() / 2 - 100);
        RenderCompat.disableBlend();
    }

    public static int renderClassicArmor(GuiGraphics graphics, int rightHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isClassicWithoutHunger() || isSurvivalHudHidden(minecraft)) {
            return rightHeight;
        }
        prepareBlend();
        HudHelper.renderArmor(graphics, rightHeight, graphics.guiWidth() / 2 + 90);
        RenderCompat.disableBlend();
        return HudHelper.isArmorEmpty() ? rightHeight : rightHeight + 10;
    }

    public static int renderClassicStamina(GuiGraphics graphics, int rightHeight) {
        if (!isClassicWithoutHunger()) {
            return rightHeight;
        }
        return renderStamina(graphics, rightHeight);
    }

    public static int renderDefaultStamina(GuiGraphics graphics, int rightHeight) {
        if (isClassicWithoutHunger()) {
            return rightHeight;
        }
        return renderStamina(graphics, rightHeight);
    }

    private static int renderStamina(GuiGraphics graphics, int rightHeight) {
        if (!StaminaRenderer.isVisible()) {
            return rightHeight;
        }
        prepareBlend();
        StaminaRenderer.render(graphics, rightHeight, 0);
        RenderCompat.disableBlend();
        return rightHeight + StaminaRenderer.occupiedHeight();
    }

    private static void prepareBlend() {
        RenderCompat.prepareHud();
    }
}
