package com.evandev.modest_meals.stamina;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public enum Corner {
    TOP_LEFT("config.modest_meals.enum.corner.top_left"),
    TOP_RIGHT("config.modest_meals.enum.corner.top_right"),
    BOTTOM_LEFT("config.modest_meals.enum.corner.bottom_left"),
    BOTTOM_RIGHT("config.modest_meals.enum.corner.bottom_right");

    private final String key;

    Corner(String key) {
        this.key = key;
    }

    public Component getTitle() {
        return Component.translatable(this.key);
    }

    public void drawText(GuiGraphics graphics, String text, int offsetX, int offsetY, boolean shadow) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int textWidth = font.width(text);
        int textHeight = font.lineHeight;

        int x = 0;
        int y = 0;

        switch (this) {
            case TOP_LEFT -> {
                x = 4 + offsetX;
                y = 4 + offsetY;
            }
            case TOP_RIGHT -> {
                x = screenWidth - textWidth - 4 + offsetX;
                y = 4 + offsetY;
            }
            case BOTTOM_LEFT -> {
                x = 4 + offsetX;
                y = screenHeight - textHeight - 4 + offsetY;
            }
            case BOTTOM_RIGHT -> {
                x = screenWidth - textWidth - 4 + offsetX;
                y = screenHeight - textHeight - 4 + offsetY;
            }
        }

        graphics.drawString(font, text, x, y, 0xFFFFFF, shadow);
    }
}
