package com.evandev.modest_meals.client.gui;

import com.evandev.modest_meals.client.gui.editor.FormBuilder;
import com.evandev.modest_meals.client.gui.editor.TraitEditor;
import com.evandev.modest_meals.client.gui.editor.TraitEditorRegistry;
import com.evandev.modest_meals.client.gui.util.GuiUtil;
import com.evandev.modest_meals.client.gui.widget.DropdownWidget;
import com.evandev.modest_meals.client.gui.widget.ModButton;
import com.evandev.modest_meals.client.tooltip.FoodItemTooltips;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TraitEditDialog extends Screen {
    private static final int PANEL_W = 300;
    private static final int TITLE_H = 22;
    private static final int ROW_H = 18;
    private static final int GAP = 8;
    private static final int PREVIEW_H = 34;
    private static final int FOOTER_H = 28;

    private final Screen parent;
    private final Consumer<FoodTrait> onSave;
    private final List<FoodTraitType<?>> availableTypes = new ArrayList<>();

    private final Map<String, Object> carried = new HashMap<>();

    private FoodTraitType<?> currentType;
    private TraitEditor<?> currentEditor;
    private FormBuilder form;
    private DropdownWidget typeDropdown;
    private Button doneButton;

    private double scroll = 0;
    private boolean draggingScrollbar = false;

    private FoodTrait previewTrait;

    private int panelX, panelY, panelH;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public TraitEditDialog(Screen parent, Consumer<FoodTrait> onSave, FoodTrait initialTrait) {
        super(Component.translatable("gui.modest_meals.trait_dialog.title"));
        this.parent = parent;
        this.onSave = onSave;

        for (FoodTraitType<?> type : FoodTraitType.REGISTRY) {
            this.availableTypes.add(type);
        }

        if (initialTrait != null) {
            this.currentType = initialTrait.getType();
            this.currentEditor = TraitEditorRegistry.getEditor((FoodTraitType) this.currentType);
            ((TraitEditor) this.currentEditor).initFrom(initialTrait);
        } else if (!availableTypes.isEmpty()) {
            this.currentType = availableTypes.getFirst();
            this.currentEditor = TraitEditorRegistry.getEditor((FoodTraitType) this.currentType);
        }
    }

    private static String typeId(FoodTraitType<?> type) {
        ResourceLocation key = FoodTraitType.REGISTRY.getKey(type);
        return key != null ? key.toString() : "modest_meals:unknown";
    }

    private static Component typeLabel(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        if (key == null) return Component.translatable("modest_meals.trait.unknown");
        return Component.translatable(key.getNamespace() + ".trait." + key.getPath());
    }

    private int formViewTop() {
        return panelY + TITLE_H + ROW_H + GAP * 2;
    }

    private int formViewHeight() {
        return Math.max(ROW_H, panelY + panelH - FOOTER_H - PREVIEW_H - GAP - formViewTop());
    }

    private int formWidth() {
        return PANEL_W - GuiUtil.PANEL_PADDING * 2 - GuiUtil.SCROLLBAR_EXTRA_WIDTH;
    }

    private int maxScroll() {
        return form == null ? 0 : Math.max(0, form.getHeight() - formViewHeight());
    }

    @Override
    protected void init() {
        super.init();

        int desired = TITLE_H + ROW_H + GAP * 2 + 160 + PREVIEW_H + GAP + FOOTER_H;
        this.panelH = Math.min(desired, this.height - 20);
        this.panelX = (this.width - PANEL_W) / 2;
        this.panelY = (this.height - panelH) / 2;

        rebuild();
    }

    private void rebuild() {
        this.clearWidgets();

        int contentX = panelX + GuiUtil.PANEL_PADDING;
        int contentW = PANEL_W - GuiUtil.PANEL_PADDING * 2;

        List<String> typeIds = availableTypes.stream().map(TraitEditDialog::typeId).toList();
        this.typeDropdown = new DropdownWidget(contentX, panelY + TITLE_H + GAP, contentW, ROW_H,
                Component.empty(), typeIds, TraitEditDialog::typeLabel, this::onTypeSelected)
                .searchable("gui.modest_meals.search.traits");
        this.typeDropdown.setSelectedValue(currentType != null ? typeId(currentType) : null);
        this.addRenderableWidget(this.typeDropdown);

        this.form = new FormBuilder(this.font, contentX, formViewTop(), formWidth(), this::onFormChanged, carried);
        if (currentEditor != null) {
            currentEditor.buildForm(this.form);
        }
        this.form.widgets().forEach(this::addRenderableWidget);

        this.scroll = Mth.clamp(this.scroll, 0, maxScroll());

        int footerY = panelY + panelH - FOOTER_H + 4;
        int buttonW = (contentW - GAP) / 2;
        this.doneButton = this.addRenderableWidget(new ModButton(contentX, footerY, buttonW, 20,
                Component.translatable("gui.done"), b -> save()));
        this.addRenderableWidget(new ModButton(contentX + buttonW + GAP, footerY, buttonW, 20,
                CommonComponents.GUI_CANCEL, b -> this.minecraft.setScreen(this.parent)));

        onFormChanged();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void onTypeSelected(String id) {
        FoodTraitType<?> next = null;
        for (FoodTraitType<?> type : availableTypes) {
            if (typeId(type).equals(id)) {
                next = type;
                break;
            }
        }
        if (next == null || next.equals(currentType)) return;

        if (form != null) carried.putAll(form.values());

        this.currentType = next;
        this.currentEditor = TraitEditorRegistry.getEditor((FoodTraitType) next);
        this.scroll = 0;
        rebuild();
    }

    private void onFormChanged() {
        this.previewTrait = currentEditor != null ? currentEditor.createTrait() : null;
        if (this.doneButton != null) {
            this.doneButton.active = form != null && form.isValid() && this.previewTrait != null;
        }
    }

    private void save() {
        if (form != null && !form.isValid()) return;
        if (currentEditor != null) {
            FoodTrait constructed = currentEditor.createTrait();
            if (constructed != null) onSave.accept(constructed);
        }
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (typeDropdown != null && typeDropdown.isOpen()) {
            if (typeDropdown.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(typeDropdown);
                return true;
            }
        }
        for (DropdownWidget d : form.dropdowns()) {
            if (d.isOpen() && d.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(d);
                return true;
            }
        }

        if (button == 0 && isOverScrollbar(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            this.scroll = GuiUtil.scrollAmountFromMouse(mouseY, formViewTop(), formViewHeight(), maxScroll());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        if (maxScroll() <= 0) return false;
        int barX = panelX + GuiUtil.PANEL_PADDING + formWidth();
        return mouseX >= barX && mouseX < barX + GuiUtil.SCROLLBAR_WIDTH
                && mouseY >= formViewTop() && mouseY < formViewTop() + formViewHeight();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            this.scroll = GuiUtil.scrollAmountFromMouse(mouseY, formViewTop(), formViewHeight(), maxScroll());
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (typeDropdown != null && typeDropdown.isOpen() && typeDropdown.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        for (DropdownWidget d : form.dropdowns()) {
            if (d.isOpen() && d.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        }
        if (maxScroll() > 0) {
            this.scroll = Mth.clamp(this.scroll - scrollY * 12, 0, maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (typeDropdown != null && typeDropdown.isOpen() && typeDropdown.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        for (DropdownWidget d : form.dropdowns()) {
            if (d.isOpen() && d.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (typeDropdown != null && typeDropdown.isOpen() && typeDropdown.charTyped(codePoint, modifiers)) {
            return true;
        }
        for (DropdownWidget d : form.dropdowns()) {
            if (d.isOpen() && d.charTyped(codePoint, modifiers)) return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        GuiUtil.drawDialogPanel(graphics, panelX, panelY, PANEL_W, panelH);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int viewTop = formViewTop();
        int viewBottom = viewTop + formViewHeight();
        form.applyScroll(scroll, viewTop, viewBottom);

        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(this.font, this.title, panelX + PANEL_W / 2, panelY + 8, GuiUtil.WHITE);

        graphics.enableScissor(panelX + GuiUtil.PANEL_PADDING, viewTop,
                panelX + GuiUtil.PANEL_PADDING + formWidth(), viewBottom);
        form.renderLabels(graphics, scroll);
        graphics.disableScissor();

        if (maxScroll() > 0) {
            GuiUtil.drawVanillaScrollbar(graphics, panelX + GuiUtil.PANEL_PADDING + formWidth(),
                    viewTop, formViewHeight(), scroll, maxScroll());
        }

        renderPreview(graphics, viewBottom);

        if (typeDropdown != null) typeDropdown.renderOverlay(graphics, mouseX, mouseY);
        for (DropdownWidget d : form.dropdowns()) {
            d.renderOverlay(graphics, mouseX, mouseY);
        }
    }

    private void renderPreview(GuiGraphics graphics, int viewBottom) {
        int contentX = panelX + GuiUtil.PANEL_PADDING;
        int contentW = PANEL_W - GuiUtil.PANEL_PADDING * 2;
        int previewY = viewBottom + GAP;

        graphics.drawString(this.font, Component.translatable("gui.modest_meals.tooltip_preview"),
                contentX, previewY, GuiUtil.LABEL);

        int boxY = previewY + 11;
        graphics.fill(contentX, boxY, contentX + contentW, boxY + 20, 0x88000000);

        if (form != null && !form.isValid()) {
            GuiUtil.drawTrimmed(graphics, this.font, Component.translatable("gui.modest_meals.invalid_input"),
                    contentX + 4, boxY + 6, contentW - 8, GuiUtil.ERROR_RED);
        } else if (previewTrait != null) {
            Component tooltip = FoodItemTooltips.hasTooltipLine(previewTrait)
                    ? previewTrait.getTooltipComponent(1.0, 1.0)
                    : null;
            if (tooltip != null) {
                GuiUtil.drawTrimmed(graphics, this.font, tooltip, contentX + 4, boxY + 6, contentW - 8, GuiUtil.WHITE);
            } else {
                GuiUtil.drawTrimmed(graphics, this.font,
                        Component.translatable("gui.modest_meals.tooltip_preview.none")
                                .withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY),
                        contentX + 4, boxY + 6, contentW - 8, GuiUtil.DIM);
            }
        }
    }
}
