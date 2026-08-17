package com.evandev.modest_meals.client;

import com.evandev.modest_meals.stamina.Corner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public abstract class CornerTextRenderer {
    public static void drawText(Corner corner, GuiGraphics graphics, String text, int offsetX, int offsetY, boolean shadow) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int textWidth = font.width(text);
        int textHeight = font.lineHeight;

        int x = 0;
        int y = 0;

        switch (corner) {
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
