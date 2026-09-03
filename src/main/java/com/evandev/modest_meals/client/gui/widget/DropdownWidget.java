package com.evandev.modest_meals.client.gui.widget;

import com.evandev.modest_meals.client.gui.util.GuiUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public class DropdownWidget extends AbstractWidget {
    private static final int ROW_H = 12;
    private static final int MAX_VISIBLE_ROWS = 10;
    private static final int SEARCH_H = 14;
    private static final int SEARCH_GAP = 2;

    private final List<String> values = new ArrayList<>();
    private final List<String> visible = new ArrayList<>();
    private final Function<String, Component> labeller;
    private final Consumer<String> onSelect;
    private final Component title;

    private String selectedValue;
    private boolean open = false;
    private boolean searchable = false;
    private String filter = "";
    private double scrollAmount = 0;

    public DropdownWidget(int x, int y, int width, int height, Component title,
                          List<String> values, Function<String, Component> labeller, Consumer<String> onSelect) {
        super(x, y, width, height, title);
        this.title = title;
        this.labeller = labeller;
        this.onSelect = onSelect;
        this.values.addAll(values);
        this.selectedValue = this.values.isEmpty() ? null : this.values.getFirst();
        refilter();
    }

    public DropdownWidget searchable() {
        this.searchable = true;
        return this;
    }

    public void setValues(List<String> newValues) {
        this.values.clear();
        this.values.addAll(newValues);
        if (selectedValue == null || !this.values.contains(selectedValue)) {
            this.selectedValue = this.values.isEmpty() ? null : this.values.getFirst();
        }
        this.scrollAmount = 0;
        refilter();
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(String value) {
        if (values.contains(value)) this.selectedValue = value;
    }

    public boolean isOpen() {
        return open;
    }

    public void close() {
        this.open = false;
        this.filter = "";
        refilter();
    }

    private void refilter() {
        visible.clear();
        if (filter.isEmpty()) {
            visible.addAll(values);
        } else {
            String q = filter.toLowerCase(Locale.ROOT);
            for (String v : values) {
                if (labelFor(v).getString().toLowerCase(Locale.ROOT).contains(q)
                        || v.toLowerCase(Locale.ROOT).contains(q)) {
                    visible.add(v);
                }
            }
        }
        this.scrollAmount = Mth.clamp(this.scrollAmount, 0, maxScroll());
    }

    private Component labelFor(String value) {
        if (value == null) return Component.empty();
        return labeller != null ? labeller.apply(value) : Component.literal(value);
    }

    private int visibleRows() {
        return Math.min(MAX_VISIBLE_ROWS, Math.max(1, visible.size()));
    }

    private int rowsHeight() {
        return visibleRows() * ROW_H;
    }

    private int searchOffset() {
        return searchable ? SEARCH_H + SEARCH_GAP : 0;
    }

    private int contentX() {
        return this.getX() + GuiUtil.PANEL_PADDING;
    }

    private int contentY() {
        return this.getY() + this.height + 2 + GuiUtil.PANEL_PADDING;
    }

    private int contentWidth() {
        return this.width - GuiUtil.PANEL_PADDING * 2 - GuiUtil.SCROLLBAR_EXTRA_WIDTH;
    }

    private int contentHeight() {
        return searchOffset() + rowsHeight();
    }

    private int rowsY() {
        return contentY() + searchOffset();
    }

    private int maxScroll() {
        return Math.max(0, visible.size() * ROW_H - rowsHeight());
    }

    public boolean isOverList(double mouseX, double mouseY) {
        if (!open) return false;
        int x0 = this.getX();
        int x1 = contentX() + contentWidth() + GuiUtil.SCROLLBAR_WIDTH;
        int y0 = contentY() - GuiUtil.PANEL_PADDING;
        int y1 = contentY() + contentHeight() + GuiUtil.PANEL_PADDING;
        return mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;
    }

    private int rowIndexAt(double mouseX, double mouseY) {
        int cx = contentX();
        int cy = rowsY();
        if (mouseX < cx || mouseX >= cx + contentWidth()) return -1;
        if (mouseY < cy || mouseY >= cy + rowsHeight()) return -1;
        int idx = (int) ((mouseY - cy + scrollAmount) / ROW_H);
        return idx >= 0 && idx < visible.size() ? idx : -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        if (open) {
            int idx = rowIndexAt(mouseX, mouseY);
            if (idx >= 0) {
                select(visible.get(idx));
                return true;
            }
            if (isOverList(mouseX, mouseY)) return true;
            close();
            return clicked(mouseX, mouseY);
        }

        if (clicked(mouseX, mouseY)) {
            this.open = true;
            this.filter = "";
            refilter();
            scrollToSelected();
            setFocused(true);
            return true;
        }
        return false;
    }

    private void select(String value) {
        this.selectedValue = value;
        close();
        if (onSelect != null) onSelect.accept(value);
    }

    private void scrollToSelected() {
        int idx = visible.indexOf(selectedValue);
        if (idx < 0) {
            this.scrollAmount = 0;
            return;
        }
        this.scrollAmount = Mth.clamp(idx * ROW_H - rowsHeight() / 2.0, 0, maxScroll());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (open && isOverList(mouseX, mouseY)) {
            this.scrollAmount = Mth.clamp(this.scrollAmount - scrollY * ROW_H, 0, maxScroll());
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!open) return false;
        switch (keyCode) {
            case GLFW.GLFW_KEY_ESCAPE -> {
                close();
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (searchable && !filter.isEmpty()) {
                    filter = filter.substring(0, filter.length() - 1);
                    refilter();
                }
                return true;
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                if (!visible.isEmpty()) select(visible.getFirst());
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!open || !searchable || !StringUtil.isAllowedChatCharacter(codePoint)) return false;
        filter += codePoint;
        this.scrollAmount = 0;
        refilter();
        return true;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.enableBlend();
        guiGraphics.blitSprite(this.isHovered || open ? GuiUtil.TEXT_FIELD_HIGHLIGHTED : GuiUtil.TEXT_FIELD,
                this.getX(), this.getY(), this.width, this.height);
        RenderSystem.disableBlend();

        var font = Minecraft.getInstance().font;
        Component label = Component.empty().append(title).append(labelFor(selectedValue));
        int textY = this.getY() + (this.height - 8) / 2;

        guiGraphics.drawString(font, GuiUtil.trim(font, label.getString(), this.width - 8),
                this.getX() + 4, textY, GuiUtil.WHITE);
    }

    public void renderOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!open) return;

        int cx = contentX();
        int cy = contentY();
        int cw = contentWidth();
        int ch = contentHeight();
        var font = Minecraft.getInstance().font;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300);

        GuiUtil.drawContentPanel(guiGraphics, cx, cy, cw, ch);

        if (searchable) {
            RenderSystem.enableBlend();
            guiGraphics.blitSprite(GuiUtil.TEXT_FIELD_HIGHLIGHTED, cx, cy, cw, SEARCH_H);
            RenderSystem.disableBlend();
            boolean empty = filter.isEmpty();
            String shown = empty ? Component.translatable("gui.modest_meals.search").getString() : filter + "_";
            guiGraphics.drawString(font, GuiUtil.trim(font, shown, cw - 8),
                    cx + 4, cy + (SEARCH_H - 8) / 2, empty ? 0xFF808080 : GuiUtil.WHITE);
        }

        int rowsY = rowsY();
        int rowsH = rowsHeight();
        int hovered = rowIndexAt(mouseX, mouseY);

        if (visible.isEmpty()) {
            guiGraphics.drawString(font, Component.translatable("gui.modest_meals.no_matches"),
                    cx + 3, rowsY + 2, GuiUtil.EMPTY_STATE);
        } else {
            guiGraphics.enableScissor(cx, rowsY, cx + cw, rowsY + rowsH);
            int first = (int) (scrollAmount / ROW_H);
            int offset = (int) (scrollAmount % ROW_H);
            for (int r = 0; r <= visibleRows(); r++) {
                int idx = first + r;
                if (idx < 0 || idx >= visible.size()) continue;

                int rowY = rowsY + r * ROW_H - offset;
                String value = visible.get(idx);
                if (value.equals(selectedValue)) {
                    guiGraphics.fill(cx, rowY, cx + cw, rowY + ROW_H, GuiUtil.SELECTED_SLOT);
                } else if (idx == hovered) {
                    guiGraphics.fill(cx, rowY, cx + cw, rowY + ROW_H, 0x33FFFFFF);
                }
                guiGraphics.drawString(font, GuiUtil.trim(font, labelFor(value).getString(), cw - 6),
                        cx + 3, rowY + 2, GuiUtil.WHITE);
            }
            guiGraphics.disableScissor();
        }

        GuiUtil.drawVanillaScrollbar(guiGraphics, cx + cw, rowsY, rowsH, scrollAmount, maxScroll());

        guiGraphics.pose().popPose();
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE,
                Component.empty().append(title).append(labelFor(selectedValue)));
    }
}
