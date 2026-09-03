package com.evandev.modest_meals.client.gui.util;

import com.evandev.modest_meals.Constants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GuiUtil {
    public static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/background.png");
    public static final ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/widgets.png");

    public static final ResourceLocation SCROLLBAR_BACKGROUND = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "widget/scrollbar_background_vanilla");
    public static final ResourceLocation SCROLLBAR_TRACK = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "widget/scrollbar_track_vanilla");
    public static final ResourceLocation SCROLLBAR_THUMB = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "widget/scrollbar_thumb_vanilla");

    public static final ResourceLocation TEXT_FIELD = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "widget/text_field");
    public static final ResourceLocation TEXT_FIELD_HIGHLIGHTED = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "widget/text_field_highlighted");

    public static final ResourceLocation ROW = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "widget/tabs/vertical_vanilla");
    public static final ResourceLocation ROW_SELECTED = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "widget/tabs/vertical_vanilla_selected");

    public static final int PANEL_PADDING = 9;
    public static final int SCROLLBAR_WIDTH = 16;

    public static final int SCROLLBAR_EXTRA_WIDTH = SCROLLBAR_WIDTH - PANEL_PADDING;
    public static final int ROW_INSET = 4;
    public static final int LINE_H = 10;
    public static final int SELECTED_SLOT = 0x7700BBFF;
    public static final int ADDED_SLOT = 0x3300FF00;
    public static final int INSERTION_MARKER = 0xFF00FFFF;
    public static final int PRIMARY_SELECTION_OUTLINE = 0xFF88DDFF;
    public static final int SUBTEXT = 0xB5B5B5;
    public static final int BADGE_CUSTOM = 0x88CC88;
    public static final int BADGE_MODIFIED = 0xCCAA66;
    public static final int BADGE_HIDDEN = 0xCC7777;
    public static final int HEADER_BG = 0xDD181818;
    public static final int HEADER_OUTLINE = 0xFF333333;
    public static final int SEPARATOR = 0x55FFFFFF;
    public static final int HOVER_WASH = 0x18FFFFFF;
    public static final int LABEL = 0xDDDDDD;
    public static final int DIM = 0x777777;
    public static final int EMPTY_STATE = 0x888888;
    public static final int STATUS_GREEN = 0x55FF55;
    public static final int ERROR_RED = 0xFF5555;
    public static final int WHITE = 0xFFFFFF;
    private static final int TRACK_PADDING = 2;

    public static void drawNinePatch(GuiGraphics context, ResourceLocation texture, int x, int y, int w, int h, int u, int v, int cornerLength, int centerLength) {
        int corcen = cornerLength + centerLength;
        int innerWidth = w - cornerLength * 2;
        int innerHeight = h - cornerLength * 2;
        int coriw = cornerLength + innerWidth;
        int corih = cornerLength + innerHeight;

        context.blit(texture, x, y, cornerLength, cornerLength, u, v, cornerLength, cornerLength, 256, 256);
        context.blit(texture, x + cornerLength, y, innerWidth, cornerLength, u + cornerLength, v, centerLength, cornerLength, 256, 256);
        context.blit(texture, x + coriw, y, cornerLength, cornerLength, u + corcen, v, cornerLength, cornerLength, 256, 256);
        context.blit(texture, x, y + cornerLength, cornerLength, innerHeight, u, v + cornerLength, cornerLength, centerLength, 256, 256);
        context.blit(texture, x + cornerLength, y + cornerLength, innerWidth, innerHeight, u + cornerLength, v + cornerLength, centerLength, centerLength, 256, 256);
        context.blit(texture, x + coriw, y + cornerLength, cornerLength, innerHeight, u + corcen, v + cornerLength, cornerLength, centerLength, 256, 256);
        context.blit(texture, x, y + corih, cornerLength, cornerLength, u, v + corcen, cornerLength, cornerLength, 256, 256);
        context.blit(texture, x + cornerLength, y + corih, innerWidth, cornerLength, u + cornerLength, v + corcen, centerLength, cornerLength, 256, 256);
        context.blit(texture, x + coriw, y + corih, cornerLength, cornerLength, u + corcen, v + corcen, cornerLength, cornerLength, 256, 256);
    }

    public static void drawVanillaPanel(GuiGraphics context, int x, int y, int width, int height) {
        drawNinePatch(context, BACKGROUND, x, y, width, height, 0, 32, 8, 1);
    }

    public static void drawDialogPanel(GuiGraphics context, int x, int y, int width, int height) {
        drawNinePatch(context, BACKGROUND, x, y, width, height, 27, 0, 4, 1);
    }

    public static void drawContentPanel(GuiGraphics context, int contentX, int contentY, int contentWidth, int contentHeight) {
        drawVanillaPanel(context, contentX - PANEL_PADDING, contentY - PANEL_PADDING,
                contentWidth + PANEL_PADDING * 2, contentHeight + PANEL_PADDING * 2);
    }

    public static void drawVanillaScrollbar(GuiGraphics context, int x, int contentY, int contentHeight,
                                            double scrollAmount, int maxScroll) {
        int trackY = contentY - TRACK_PADDING;
        int trackHeight = contentHeight + TRACK_PADDING * 2;

        RenderSystem.enableBlend();
        context.blitSprite(SCROLLBAR_BACKGROUND, x, contentY - PANEL_PADDING, SCROLLBAR_WIDTH, contentHeight + PANEL_PADDING * 2);
        context.blitSprite(SCROLLBAR_TRACK, x, trackY, SCROLLBAR_WIDTH, trackHeight);

        int thumbHeight = trackHeight;
        int thumbY = trackY;
        if (maxScroll > 0) {
            thumbHeight = thumbHeight(trackHeight, contentHeight, maxScroll);
            thumbY = trackY + (int) ((trackHeight - thumbHeight) * (scrollAmount / maxScroll));
        }
        context.blitSprite(SCROLLBAR_THUMB, x, thumbY, SCROLLBAR_WIDTH, thumbHeight);
        RenderSystem.disableBlend();
    }

    private static int thumbHeight(int trackHeight, int contentHeight, int maxScroll) {
        float visibleFraction = (float) contentHeight / (float) (contentHeight + maxScroll);
        return Math.max(SCROLLBAR_WIDTH, (int) (trackHeight * visibleFraction));
    }

    public static double scrollAmountFromMouse(double mouseY, int contentY, int contentHeight, int maxScroll) {
        if (maxScroll <= 0) return 0;
        int trackY = contentY - TRACK_PADDING;
        int trackHeight = contentHeight + TRACK_PADDING * 2;
        int thumbHeight = thumbHeight(trackHeight, contentHeight, maxScroll);
        int travel = trackHeight - thumbHeight;
        if (travel <= 0) return 0;

        double fraction = (mouseY - trackY - thumbHeight / 2.0) / travel;
        return Math.max(0, Math.min(1, fraction)) * maxScroll;
    }

    public static void drawSlot(GuiGraphics context, int x, int y) {
        context.blit(WIDGETS, x, y, 0, 0, 18, 18, 256, 256);
    }

    public static void drawSlotHighlight(GuiGraphics context, int x, int y) {
        context.fill(x, y, x + 18, y + 18, 0x66FFFFFF);
    }

    public static void drawRow(GuiGraphics context, int x, int y, int width, int height, boolean selected, boolean hovered) {
        RenderSystem.enableBlend();
        context.blitSprite(selected ? ROW_SELECTED : ROW, x, y, width, height);
        if (hovered && !selected) {
            context.fill(x + 1, y + 1, x + width - 1, y + height - 1, HOVER_WASH);
        }
        RenderSystem.disableBlend();
    }

    public static String trim(Font font, String text, int maxWidth) {
        if (maxWidth <= 0) return "";
        if (font.width(text) <= maxWidth) return text;
        int ellipsis = font.width("..");
        if (maxWidth <= ellipsis) return "";
        return font.plainSubstrByWidth(text, maxWidth - ellipsis) + "..";
    }

    public static FormattedText trim(Font font, FormattedText text, int maxWidth) {
        if (maxWidth <= 0) return FormattedText.EMPTY;
        if (font.width(text) <= maxWidth) return text;
        int ellipsis = font.width("..");
        if (maxWidth <= ellipsis) return FormattedText.EMPTY;
        return FormattedText.composite(font.substrByWidth(text, maxWidth - ellipsis), FormattedText.of(".."));
    }

    public static void drawTrimmed(GuiGraphics context, Font font, Component text, int x, int y, int maxWidth, int color) {
        context.drawString(font, Language.getInstance().getVisualOrder(trim(font, text, maxWidth)), x, y, color);
    }

    public static List<String> wrap(Font font, String text, int maxWidth, int maxLines) {
        if (maxWidth <= 0 || maxLines <= 0) return List.of();

        List<FormattedText> split = font.getSplitter().splitLines(text, maxWidth, Style.EMPTY);
        if (split.isEmpty()) return List.of();

        List<String> lines = new ArrayList<>();
        for (int i = 0; i < Math.min(split.size(), maxLines); i++) {
            lines.add(split.get(i).getString());
        }
        if (split.size() > maxLines) {
            StringBuilder rest = new StringBuilder(lines.get(maxLines - 1));
            for (int i = maxLines; i < split.size(); i++) rest.append(' ').append(split.get(i).getString());
            lines.set(maxLines - 1, trim(font, rest.toString(), maxWidth));
        }
        return lines;
    }

    public static int drawWrapped(GuiGraphics context, Font font, Component text, int x, int y,
                                  int maxWidth, int maxLines, int color) {
        List<String> lines = wrap(font, text.getString(), maxWidth, maxLines);
        for (int i = 0; i < lines.size(); i++) {
            context.drawString(font, lines.get(i), x, y + i * LINE_H, color);
        }
        return lines.size() * LINE_H;
    }

    public static void drawWrappedCentered(GuiGraphics context, Font font, Component text, int centerX, int centerY,
                                           int maxWidth, int maxLines, int color) {
        List<String> lines = wrap(font, text.getString(), maxWidth, maxLines);
        int top = centerY - lines.size() * LINE_H / 2;
        for (int i = 0; i < lines.size(); i++) {
            context.drawCenteredString(font, lines.get(i), centerX, top + i * LINE_H, color);
        }
    }

    public static void drawSeparator(GuiGraphics context, int x, int y, int width) {
        context.fill(x, y, x + width, y + 1, SEPARATOR);
    }
}
