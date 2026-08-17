package com.evandev.modest_meals.client;

import com.evandev.modest_meals.client.hud.PreviewFood;
import com.evandev.modest_meals.client.hud.StaminaSprites;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.stamina.PlayerStamina;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.awt.*;

public abstract class StaminaRenderer {
    private static final int ICONS_PER_ROW = 10;
    private static final int ICON_SIZE = 9;

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

    public static int iconCount(PlayerStamina stamina) {
        return Mth.ceil(stamina.getMaxLevel() / 2.0F);
    }

    public static int rowCount(PlayerStamina stamina) {
        return Mth.ceil(iconCount(stamina) / (float) ICONS_PER_ROW);
    }

    private static int rowHeight(int rows) {
        return Math.max(10 - (rows - 2), 3);
    }

    public static int occupiedHeight() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 10;
        }
        PlayerStamina stamina = StaminaHelper.get(player);
        int rows = rowCount(stamina);
        return 10 + (rows - 1) * rowHeight(rows);
    }

    public static ResourceLocation getSprite(PlayerStamina stamina, int icon) {
        ResourceLocation sprite = stamina.getData().isExhausted() ? ModSprites.STAMINA_RECHARGE : ModSprites.STAMINA_LEVEL;

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
        int maxLevel = stamina.getMaxLevel();

        if (ModConfig.get().highlightStaminaBar && level != maxLevel) {
            shouldHighlight = stamina.isAtFullSprint();
        }

        long now = System.currentTimeMillis();

        if (hasBegunToDrain && level == maxLevel) {
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
        } else if (level < maxLevel) {
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

        float addedSeconds = PreviewFood.staminaSeconds(player);
        if (addedSeconds <= 0.0F) {
            return level;
        }

        int fullBarInTicks = stamina.getData().isExhausted() ? stamina.getRechargeInTicks() : stamina.getDurationInTicks();
        if (fullBarInTicks <= 0) {
            return level;
        }

        int maxLevel = stamina.getMaxLevel();
        int previewTicks = Math.min(fullBarInTicks, stamina.getData().getRemaining() + (int) (addedSeconds * 20));
        int previewLevel = Mth.clamp(
                (int) Math.ceil((double) previewTicks / fullBarInTicks * maxLevel),
                0, maxLevel
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
        int baseTop = height - rightHeight;
        int level = stamina.getData().getStamina();
        int maxLevel = stamina.getMaxLevel();
        int previewLevel = getPreviewLevel(player, stamina);

        int icons = iconCount(stamina);
        int rows = rowCount(stamina);
        int rowHeight = rowHeight(rows);

        int bounceIcon = stamina.hasPositiveEffect()
                ? minecraft.gui.getGuiTicks() % Mth.ceil(icons + 5.0F)
                : -1;

        if (ModConfig.get().flashStaminaBarWhenFull && level < maxLevel) {
            hasBegunToDrain = true;
        }

        boolean highlight = shouldHighlight(stamina);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (int i = 0; i < icons; i++) {
            int x = left - (i % ICONS_PER_ROW) * 8 - ICON_SIZE;
            int top = baseTop - (i / ICONS_PER_ROW) * rowHeight;
            if (i == bounceIcon) {
                top -= 2;
            }

            int icon = i * 2 + 1;
            ResourceLocation sprite = getSprite(stamina, icon);

            graphics.blitSprite(ModSprites.STAMINA_EMPTY, x, top, ICON_SIZE, ICON_SIZE);
            graphics.blitSprite(sprite, x, top, ICON_SIZE, ICON_SIZE);

            renderPreview(graphics, x, top, icon, level, previewLevel);

            if (highlight) {
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 1.0F);
                graphics.blitSprite(ModSprites.STAMINA_HIGHLIGHT, x, top, ICON_SIZE, ICON_SIZE);
                graphics.pose().popPose();
            }
        }

        RenderSystem.disableBlend();
    }

    private static void renderPreview(GuiGraphics graphics, int x, int y, int icon, int level, int previewLevel) {
        if (previewLevel <= level) {
            return;
        }

        if (level % 2 != 0 && icon == level) {
            drawPreviewTexture(graphics, x, y, StaminaSprites.STAMINA_LEVEL_OTHER_HALF);
            return;
        }

        if (icon > level && icon <= previewLevel) {
            boolean isHalf = icon == previewLevel && previewLevel % 2 != 0;
            drawPreviewSprite(graphics, x, y, isHalf ? ModSprites.STAMINA_LEVEL_HALF : ModSprites.STAMINA_LEVEL);
        }
    }

    private static void drawPreviewSprite(GuiGraphics graphics, int x, int y, ResourceLocation sprite) {
        withPreviewColor(graphics, () -> graphics.blitSprite(sprite, x, y, ICON_SIZE, ICON_SIZE));
    }

    private static void drawPreviewTexture(GuiGraphics graphics, int x, int y, ResourceLocation texture) {
        if (!StaminaSprites.isOtherHalfAvailable()) {
            return;
        }
        withPreviewColor(graphics, () -> graphics.blit(texture, x, y, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE));
    }

    private static void withPreviewColor(GuiGraphics graphics, Runnable draw) {
        Color color = ModConfig.get().restoredStaminaOverlayColor;
        float[] rgba = color.getRGBComponents(null);
        graphics.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
        draw.run();
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
