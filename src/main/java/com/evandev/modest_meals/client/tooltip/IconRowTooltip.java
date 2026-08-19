package com.evandev.modest_meals.client.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
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

public class IconRowTooltip implements ClientTooltipComponent {
    public static final int MAX_ICONS = 10;
    public static final int ICON_WIDTH = 9;
    public static final int SECTION_SPACING = 4;
    private final List<Section> sections;

    private IconRowTooltip(List<Section> sections) {
        this.sections = sections;
    }

    public static IconRowTooltip of(Sprites sprites, int halfUnits) {
        return of(List.of(new Entry(sprites, halfUnits)));
    }

    public static IconRowTooltip of(List<Entry> entries) {
        List<Section> sections = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            sections.add(entry.toSection());
        }
        return new IconRowTooltip(sections);
    }

    private static void draw(GuiGraphics context, Icon icon, int x, int y) {
        if (icon.isAtlasSprite()) {
            context.blitSprite(icon.location(), x, y, ICON_WIDTH, ICON_WIDTH);
        } else {
            context.blit(icon.location(), x, y, 0.0F, 0.0F, ICON_WIDTH, ICON_WIDTH, ICON_WIDTH, ICON_WIDTH);
        }
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public int getWidth(Font font) {
        if (sections.isEmpty()) {
            return 0;
        }
        int totalWidth = 0;
        for (int i = 0; i < sections.size(); i++) {
            if (i > 0) {
                totalWidth += SECTION_SPACING;
            }
            totalWidth += sections.get(i).getWidth(font);
        }
        return totalWidth;
    }

    @Override
    public void renderText(Font font, int mouseX, int mouseY, org.joml.Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
        int currentX = mouseX;
        int textY = mouseY + 2;
        for (int s = 0; s < sections.size(); s++) {
            Section section = sections.get(s);
            if (s > 0) {
                currentX += SECTION_SPACING;
            }
            currentX += section.iconCount() * ICON_WIDTH;
            if (!section.text().isEmpty()) {
                font.drawInBatch(section.visualText(), (float) currentX, (float) textY, -1, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
                currentX += font.width(section.text());
            }
        }
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics context) {
        int currentX = x;
        int iconY = y + 2;
        for (int s = 0; s < sections.size(); s++) {
            Section section = sections.get(s);
            if (s > 0) {
                currentX += SECTION_SPACING;
            }
            for (int i = 0; i < section.iconCount(); i++) {
                boolean isLast = i == section.iconCount() - 1;
                boolean isHalf = isLast && section.lastIconIsHalf();
                int iconX = currentX + i * ICON_WIDTH;
                draw(context, section.sprites().background(), iconX, iconY);
                draw(context, isHalf ? section.sprites().half() : section.sprites().full(), iconX, iconY);
            }
            currentX += section.getWidth(font);
        }
    }

    public record Entry(Sprites sprites, int halfUnits) {
        public Section toSection() {
            int units = halfUnits;
            String textStr = "";
            if (units > MAX_ICONS * 2) {
                textStr = "x%d".formatted(units / 2);
                if (units % 2 > 0) {
                    textStr += ".5";
                }
                units = 2;
            }
            return new Section(
                    sprites,
                    (int) Math.ceil(units / 2.0F),
                    units % 2 != 0,
                    Component.literal(textStr).getVisualOrderText(),
                    textStr
            );
        }
    }

    public record Section(Sprites sprites, int iconCount, boolean lastIconIsHalf, FormattedCharSequence visualText,
                          String text) {
        public int getWidth(Font font) {
            return iconCount * ICON_WIDTH + (text.isEmpty() ? 0 : font.width(text));
        }
    }

    public record Icon(ResourceLocation location, boolean isAtlasSprite) {
        public static Icon sprite(ResourceLocation location) {
            return new Icon(location, true);
        }

        public static Icon texture(ResourceLocation location) {
            return new Icon(location, false);
        }
    }

    public record Sprites(Icon background, Icon full, Icon half) {
        public Sprites(ResourceLocation background, ResourceLocation full, ResourceLocation half) {
            this(Icon.sprite(background), Icon.sprite(full), Icon.sprite(half));
        }
    }

    public record Marker(List<Entry> entries) implements Component, FormattedCharSequence {
        private static final List<Component> EMPTY_SIBLINGS = new ArrayList<>();

        public Marker(Sprites sprites, int halfUnits) {
            this(List.of(new Entry(sprites, halfUnits)));
        }

        public Marker(Entry... entries) {
            this(List.of(entries));
        }

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
            return IconRowTooltip.of(entries);
        }
    }
}
