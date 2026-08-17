package com.evandev.modest_meals.client;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.food.FoodValues;
import com.evandev.modest_meals.stamina.PlayerStamina;
import com.evandev.modest_meals.stamina.StaminaData;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public abstract class StaminaRenderer {
    private static boolean hasBegunToDrain = false;
    private static long fullFlashStartTime = 0;

    public static boolean isVisible() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null || ModConfig.get().hideStaminaBar || !ModConfig.get().staminaSprint) {
            return false;
        }

        if (ModConfig.get().hideStaminaBarInactive && !StaminaHelper.get(player).isTiringOrExhausted()) {
            return false;
        }

        Entity vehicle = player.getVehicle();
        boolean isMounted = vehicle != null && vehicle.showVehicleHealth();
        boolean isInSurvival = minecraft.gameMode != null && minecraft.gameMode.canHurtPlayer();
        boolean isPlayerCamera = minecraft.getCameraEntity() instanceof Player;

        return !minecraft.options.hideGui && !isMounted && isInSurvival && isPlayerCamera;
    }

    public static ResourceLocation getSprite(PlayerStamina stamina, int icon) {
        ResourceLocation sprite = stamina.getData().isExhausted() ? ModSprites.STAMINA_RECHARGE : ModSprites.STAMINA_LEVEL;

        if (stamina.hasPositiveEffect()) {
            sprite = ModSprites.STAMINA_POSITIVE;
        }

        if (stamina.isNotRegainable() && !ModConfig.get().hideStaminaBarMoving) {
            sprite = ModSprites.STAMINA_NEGATIVE;
        }

        if (stamina.hasNegativeEffect()) {
            sprite = ModSprites.STAMINA_NEGATIVE;
        }

        if (stamina.isCoolingDown() && !ModConfig.get().hideStaminaBarCooldown) {
            sprite = ModSprites.STAMINA_COOLING;
        }

        int level = stamina.getData().getStamina();

        if (level % 2 != 0 && icon == level) {
            sprite = stamina.getData().isExhausted() ? ModSprites.STAMINA_RECHARGE_HALF : ModSprites.STAMINA_LEVEL_HALF;

            if (stamina.hasPositiveEffect()) {
                sprite = ModSprites.STAMINA_POSITIVE_HALF;
            }

            if (stamina.isNotRegainable() && !ModConfig.get().hideStaminaBarMoving) {
                sprite = ModSprites.STAMINA_NEGATIVE_HALF;
            }

            if (stamina.hasNegativeEffect()) {
                sprite = ModSprites.STAMINA_NEGATIVE_HALF;
            }

            if (stamina.isCoolingDown() && !ModConfig.get().hideStaminaBarCooldown) {
                sprite = ModSprites.STAMINA_COOLING_HALF;
            }
        } else if (icon > level) {
            sprite = ModSprites.STAMINA_EMPTY;
        }

        return sprite;
    }

    public static boolean shouldHighlight(PlayerStamina stamina) {
        boolean shouldHighlight = false;
        int level = stamina.getData().getStamina();

        if (ModConfig.get().highlightStaminaBar && level != StaminaData.MAX_STAMINA_LEVEL) {
            shouldHighlight = stamina.isAtFullSprint();
        }

        long now = System.currentTimeMillis();

        if (hasBegunToDrain && level == StaminaData.MAX_STAMINA_LEVEL) {
            if (fullFlashStartTime == 0) {
                fullFlashStartTime = now;
            }
            long elapsed = now - fullFlashStartTime;
            if (elapsed < 600) {
                shouldHighlight = (elapsed / 150) % 2 == 0;
            } else {
                hasBegunToDrain = false;
                fullFlashStartTime = 0;
            }
        } else if (level < StaminaData.MAX_STAMINA_LEVEL) {
            fullFlashStartTime = 0;
        }

        int flashAt = ModConfig.get().flashStaminaBarAt;
        if (flashAt > 0 && flashAt >= level && stamina.isAtFullSprint()) {
            shouldHighlight = (now / 200) % 2 == 0;
        }

        return shouldHighlight;
    }

    public static int getPreviewLevel(Player player, PlayerStamina stamina) {
        int level = stamina.getData().getStamina();
        if (!ModConfig.get().highlightRestoredStamina) {
            return level;
        }

        ItemStack heldFood = FoodValues.resolveHeldFood(player);
        float addedSeconds = FoodValues.staminaSeconds(heldFood);
        if (addedSeconds <= 0.0F) {
            return level;
        }

        int durationInTicks = ModConfig.get().staminaDuration * 20;
        if (durationInTicks <= 0) {
            return level;
        }

        int previewTicks = Math.min(durationInTicks, stamina.getData().getRemaining() + (int) (addedSeconds * 20));
        int previewLevel = Mth.clamp(
                (int) Math.ceil((double) previewTicks / durationInTicks * StaminaData.MAX_STAMINA_LEVEL),
                0, StaminaData.MAX_STAMINA_LEVEL
        );
        return Math.max(level, previewLevel);
    }

    public static void render(GuiGraphics graphics, int rightHeight, int offsetLeft) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null || !isVisible()) {
            return;
        }

        PlayerStamina stamina = StaminaHelper.get(player);
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int left = width / 2 + 91 + offsetLeft;
        int top = height - rightHeight;
        int level = stamina.getData().getStamina();
        int previewLevel = getPreviewLevel(player, stamina);

        if (ModConfig.get().flashStaminaBarWhenFull && level < StaminaData.MAX_STAMINA_LEVEL) {
            hasBegunToDrain = true;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (int i = 0; i < 10; i++) {
            int x = left - i * 8 - 9;
            int icon = i * 2 + 1;
            ResourceLocation sprite = getSprite(stamina, icon);

            graphics.blitSprite(ModSprites.STAMINA_EMPTY, x, top, 9, 9);
            graphics.blitSprite(sprite, x, top, 9, 9);

            if (icon > level && icon <= previewLevel) {
                renderPreviewIcon(graphics, x, top, icon == previewLevel && previewLevel % 2 != 0);
            }

            if (shouldHighlight(stamina)) {
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 1.0F);
                graphics.blitSprite(ModSprites.STAMINA_HIGHLIGHT, x, top, 9, 9);
                graphics.pose().popPose();
            }
        }

        RenderSystem.disableBlend();
    }

    private static void renderPreviewIcon(GuiGraphics graphics, int x, int y, boolean isHalf) {
        Color color = ModConfig.get().restoredStaminaOverlayColor;
        float[] rgba = color.getRGBComponents(null);
        graphics.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        graphics.blitSprite(isHalf ? ModSprites.STAMINA_LEVEL_HALF : ModSprites.STAMINA_LEVEL, x, y, 9, 9);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
