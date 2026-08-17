package com.evandev.modest_meals.client;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.stamina.PlayerStamina;
import com.evandev.modest_meals.stamina.StaminaHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public abstract class StaminaTextRenderer {
    public static String getStaminaColor(PlayerStamina stamina) {
        int level = (int) Math.floor(((double) stamina.getData().getStamina() / Math.max(1, stamina.getMaxLevel())) * 100);

        if (!ModConfig.get().useDynamicStaminaColor) {
            return String.valueOf(level);
        }

        if (stamina.getData().isExhausted()) {
            return "§7" + level + "§r";
        } else if (stamina.isCoolingDown()) {
            return "§b" + level + "§r";
        } else if (stamina.isNotRegainable() || stamina.hasNegativeEffect()) {
            return "§4" + level + "§r";
        } else if (stamina.hasPositiveEffect()) {
            return "§2" + level + "§r";
        } else {
            if (level <= 15) {
                return "§5" + level + "§r";
            } else if (level <= 25) {
                return "§d" + level + "§r";
            } else if (level <= 40) {
                return "§c" + level + "§r";
            } else if (level <= 55) {
                return "§4" + level + "§r";
            } else if (level <= 65) {
                return "§6" + level + "§r";
            } else if (level <= 75) {
                return "§e" + level + "§r";
            } else if (level <= 85) {
                return "§a" + level + "§r";
            } else {
                return "§2" + level + "§r";
            }
        }
    }

    public static String formatText(String template, String value) {
        String parsed = template.replace("%v", value);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parsed.length(); i++) {
            char c = parsed.charAt(i);
            if (c == '%' && i + 1 < parsed.length()) {
                char next = parsed.charAt(i + 1);
                if (next == '%') {
                    builder.append('%');
                    i++;
                } else if ("0123456789abcdefklmnorABCDEFKLMNOR".indexOf(next) >= 0) {
                    builder.append('§').append(next);
                    i++;
                } else {
                    builder.append(c);
                }
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null || !ModConfig.get().showStaminaText || !ModConfig.get().staminaSprint || minecraft.options.hideGui) {
            return;
        }

        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        if (minecraft.getCameraEntity() instanceof Player) {
            PlayerStamina stamina = StaminaHelper.get(player);

            if (ModConfig.get().altStaminaShowOnActive && !stamina.isTiringOrExhausted()) {
                return;
            }

            String text = formatText(ModConfig.get().altStaminaText, getStaminaColor(stamina));
            CornerTextRenderer.drawText(
                    ModConfig.get().altStaminaCorner,
                    graphics,
                    text,
                    ModConfig.get().altStaminaOffsetX,
                    ModConfig.get().altStaminaOffsetY,
                    ModConfig.get().altStaminaShadow
            );
        }
    }
}
