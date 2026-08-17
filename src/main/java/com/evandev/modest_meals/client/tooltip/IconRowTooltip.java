package com.evandev.modest_meals.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;

import java.util.ArrayList;
import java.util.List;

public class IconRowTooltip extends ClientTextTooltip {
    public static final int MAX_ICONS = 10;
    private static final int ICON_WIDTH = 9;

    private final Sprites sprites;
    private final int iconCount;
    private final boolean lastIconIsHalf;

    private IconRowTooltip(String text, Sprites sprites, int iconCount, boolean lastIconIsHalf) {
        super(Component.literal(text).getVisualOrderText());
        this.sprites = sprites;
        this.iconCount = iconCount;
        this.lastIconIsHalf = lastIconIsHalf;
    }

    /**
     * @param halfUnits the amount to show, in half-icons
     */
    public static IconRowTooltip of(Sprites sprites, int halfUnits) {
        String text = "";
        if (halfUnits > MAX_ICONS * 2) {
            text = "x%d".formatted(halfUnits / 2);
            if (halfUnits % 2 > 0) {
                text += ".5";
            }
            halfUnits = 2;
        }
        return new IconRowTooltip(text, sprites, (int) Math.ceil(halfUnits / 2.0F), halfUnits % 2 != 0);
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public int getWidth(Font font) {
        return iconCount * ICON_WIDTH + super.getWidth(font);
    }

    @Override
    public void renderText(Font font, int mouseX, int mouseY, org.joml.Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
        super.renderText(font, mouseX + 12, mouseY + 2, matrix, bufferSource);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics context) {
        y += 2;
        for (int i = 0; i < iconCount; i++) {
            boolean isLast = i == iconCount - 1;
            boolean isHalf = isLast && lastIconIsHalf;
            int iconX = x + i * ICON_WIDTH;
            context.blitSprite(sprites.background(), iconX, y, ICON_WIDTH, ICON_WIDTH);
            context.blitSprite(isHalf ? sprites.half() : sprites.full(), iconX, y, ICON_WIDTH, ICON_WIDTH);
        }
    }

    public record Sprites(ResourceLocation background, ResourceLocation full, ResourceLocation half) {
    }

    public record Marker(Sprites sprites, int halfUnits) implements Component, FormattedCharSequence {
        private static final List<Component> EMPTY_SIBLINGS = new ArrayList<>();

        @Override
        public Style getStyle() {
            return Style.EMPTY;
        }

        @Override
        public ComponentContents getContents() {
            return PlainTextContents.EMPTY;
        }

        @Override
        public List<Component> getSiblings() {
            return EMPTY_SIBLINGS;
        }

        @Override
        public FormattedCharSequence getVisualOrderText() {
            return this;
        }

        @Override
        public boolean accept(FormattedCharSink visitor) {
            return StringDecomposer.iterateFormatted(this, getStyle(), visitor);
        }

        public IconRowTooltip toTooltip() {
            return IconRowTooltip.of(sprites, halfUnits);
        }
    }
}
