package com.evandev.modest_meals.client.gui;

import com.evandev.modest_meals.client.gui.editor.FormBuilder;
import com.evandev.modest_meals.client.gui.util.GuiUtil;
import com.evandev.modest_meals.client.gui.widget.DropdownWidget;
import com.evandev.modest_meals.client.gui.widget.ModButton;
import com.evandev.modest_meals.food.ingredient.IngredientProfile;
import com.evandev.modest_meals.food.ingredient.MealEffect;
import com.evandev.modest_meals.food.ingredient.MealEffectManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class IngredientEditDialog extends Screen {
    private static final int PANEL_W = 288;
    private static final int TITLE_H = 22;
    private static final int HEADER_H = 24;
    private static final int GAP = 8;
    private static final int FOOTER_H = 28;
    private static final int FORM_ROWS = 11;

    private static final String NO_EFFECT = "";

    private final Screen parent;
    private final Item item;
    private final Consumer<IngredientProfile> onSave;

    private String effectId;
    private int potency;
    private float health;
    private float stamina;
    private int digestTicks;
    private float eatSeconds;
    private float temporaryHealth;
    private float temporaryStamina;
    private int timeBonus;

    private FormBuilder form;
    private Button doneButton;
    private int panelX, panelY, panelH;

    private double scroll = 0;
    private boolean draggingScrollbar = false;

    public IngredientEditDialog(Screen parent, Item item, IngredientProfile existing, Consumer<IngredientProfile> onSave) {
        super(Component.translatable("gui.modest_meals.ingredient_dialog.title"));
        this.parent = parent;
        this.item = item;
        this.onSave = onSave;

        IngredientProfile profile = existing == null ? IngredientProfile.EMPTY : existing;
        this.effectId = profile.effect().map(ResourceLocation::toString).orElse(NO_EFFECT);
        this.potency = profile.potency();
        this.health = profile.healthOrZero();
        this.stamina = profile.staminaOrZero();
        this.digestTicks = profile.digestTicksOrZero();
        this.eatSeconds = profile.eatSecondsOrZero();
        this.temporaryHealth = profile.temporaryHealth();
        this.temporaryStamina = profile.temporaryStamina();
        this.timeBonus = profile.timeBonusSeconds();
    }

    private static List<String> effectChoices() {
        List<String> choices = new ArrayList<>();
        choices.add(NO_EFFECT);
        MealEffectManager.all().stream()
                .map(MealEffect::id)
                .map(ResourceLocation::toString)
                .sorted()
                .forEach(choices::add);
        return choices;
    }

    private static Component effectLabel(String id) {
        if (id == null || id.isEmpty()) {
            return Component.translatable("gui.modest_meals.effect.none");
        }
        return MealEffectManager.get(ResourceLocation.parse(id))
                .map(MealEffect::displayName)
                .orElseGet(() -> Component.literal(id));
    }

    private int formViewTop() {
        return panelY + TITLE_H + HEADER_H + GAP;
    }

    private int formViewHeight() {
        return Math.max(FormBuilder.ROW_H, panelY + panelH - FOOTER_H - GAP - formViewTop());
    }

    private int formWidth() {
        return contentWidth();
    }

    private int contentWidth() {
        int full = PANEL_W - GuiUtil.PANEL_PADDING * 2;
        return maxScroll() > 0 ? full - GuiUtil.SCROLLBAR_EXTRA_WIDTH : full;
    }

    private int scrollbarX() {
        return panelX + GuiUtil.PANEL_PADDING + contentWidth();
    }

    private int scrollTrackTop() {
        return panelY + GuiUtil.PANEL_PADDING;
    }

    private int scrollTrackHeight() {
        return Math.max(1, panelH - GuiUtil.PANEL_PADDING * 2);
    }

    private int maxScroll() {
        return form == null ? 0 : Math.max(0, form.getHeight() - formViewHeight());
    }

    @Override
    protected void init() {
        super.init();

        int contentW = PANEL_W - GuiUtil.PANEL_PADDING * 2;
        int formTop = TITLE_H + HEADER_H + GAP;
        int formH = FORM_ROWS * (FormBuilder.ROW_H + 6) - 6;

        int desired = formTop + formH + GAP + FOOTER_H;
        this.panelH = Math.min(desired, this.height - 20);
        this.panelX = (this.width - PANEL_W) / 2;
        this.panelY = Math.max(4, (this.height - panelH) / 2);

        int contentX = panelX + GuiUtil.PANEL_PADDING;

        this.form = buildForm(contentX, contentW);
        if (form.getHeight() > formViewHeight()) {
            this.form = buildForm(contentX, contentW - GuiUtil.SCROLLBAR_EXTRA_WIDTH);
        }
        this.form.widgets().forEach(this::addWidget);
        this.scroll = Mth.clamp(this.scroll, 0, maxScroll());

        int footerY = panelY + panelH - FOOTER_H + 4;
        int buttonW = (contentWidth() - GAP) / 2;
        this.doneButton = this.addRenderableWidget(new ModButton(contentX, footerY, buttonW, 20,
                Component.translatable("gui.done"), b -> save()));
        this.addRenderableWidget(new ModButton(contentX + buttonW + GAP, footerY, buttonW, 20,
                CommonComponents.GUI_CANCEL, b -> this.minecraft.setScreen(this.parent)));

        onFormChanged();
    }

    private FormBuilder buildForm(int contentX, int contentW) {
        FormBuilder form = new FormBuilder(this.font, contentX, formViewTop(), contentW, this::onFormChanged);
        form.note("gui.modest_meals.section.when_eaten");
        form.decimal("gui.modest_meals.field.health", 0, 1024, health, v -> health = (float) v);
        form.decimal("gui.modest_meals.field.stamina", 0, 1024, stamina, v -> stamina = (float) v);
        form.integer("gui.modest_meals.field.digest_ticks", 0, 6000, digestTicks, v -> digestTicks = (int) v);
        form.decimal("gui.modest_meals.field.eat_seconds", 0, 60, eatSeconds, v -> eatSeconds = (float) v);
        form.note("gui.modest_meals.section.when_cooked");
        form.choice("gui.modest_meals.field.meal_effect", effectChoices(), IngredientEditDialog::effectLabel,
                effectId, v -> effectId = v, "gui.modest_meals.search.effects");
        form.integer("gui.modest_meals.field.potency", 0, 255, potency, v -> potency = (int) v);
        form.decimal("gui.modest_meals.field.temporary_health", 0, 1024, temporaryHealth,
                v -> temporaryHealth = (float) v);
        form.decimal("gui.modest_meals.field.temporary_stamina", 0, 1024, temporaryStamina,
                v -> temporaryStamina = (float) v);
        form.integer("gui.modest_meals.field.time_bonus", 0, 3600, timeBonus, v -> timeBonus = (int) v);
        return form;
    }

    private void onFormChanged() {
        if (doneButton != null) {
            doneButton.active = form == null || form.isValid();
        }
    }

    private void save() {
        if (form != null && !form.isValid()) {
            return;
        }
        Optional<ResourceLocation> effect = effectId == null || effectId.isEmpty()
                ? Optional.empty()
                : Optional.of(ResourceLocation.parse(effectId));
        onSave.accept(new IngredientProfile(
                Optional.of(health), Optional.of(stamina),
                digestTicks > 0 ? Optional.of(digestTicks) : Optional.empty(),
                eatSeconds > 0.0F ? Optional.of(eatSeconds) : Optional.empty(),
                effect, potency, temporaryHealth, temporaryStamina, timeBonus));
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (DropdownWidget dropdown : form.dropdowns()) {
            if (dropdown.isOpen() && dropdown.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(dropdown);
                return true;
            }
        }

        if (button == 0 && isOverScrollbar(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            this.scroll = GuiUtil.scrollAmountFromMouse(mouseY, scrollTrackTop(), scrollTrackHeight(),
                    formViewHeight(), maxScroll());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        if (maxScroll() <= 0) {
            return false;
        }
        int barX = scrollbarX();
        return mouseX >= barX && mouseX < barX + GuiUtil.SCROLLBAR_WIDTH
                && mouseY >= scrollTrackTop() && mouseY < scrollTrackTop() + scrollTrackHeight();
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingScrollbar) {
            this.scroll = GuiUtil.scrollAmountFromMouse(mouseY, scrollTrackTop(), scrollTrackHeight(),
                    formViewHeight(), maxScroll());
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
        for (DropdownWidget dropdown : form.dropdowns()) {
            if (dropdown.isOpen() && dropdown.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
                return true;
            }
        }
        if (maxScroll() > 0) {
            this.scroll = Mth.clamp(this.scroll - scrollY * 12, 0, maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (DropdownWidget dropdown : form.dropdowns()) {
            if (dropdown.isOpen() && dropdown.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (DropdownWidget dropdown : form.dropdowns()) {
            if (dropdown.isOpen() && dropdown.charTyped(codePoint, modifiers)) {
                return true;
            }
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

        int contentX = panelX + GuiUtil.PANEL_PADDING;
        int contentW = contentWidth();

        graphics.drawCenteredString(this.font, this.title, panelX + PANEL_W / 2, panelY + 8, GuiUtil.WHITE);

        ItemStack stack = new ItemStack(item);
        int headerY = panelY + TITLE_H;
        GuiUtil.drawSlot(graphics, contentX, headerY);
        graphics.renderFakeItem(stack, contentX + 1, headerY + 1);
        GuiUtil.drawTrimmed(graphics, this.font, stack.getHoverName(),
                contentX + 24, headerY + 5, contentW - 24, GuiUtil.WHITE);

        graphics.enableScissor(contentX, viewTop, contentX + formWidth(), viewBottom);
        form.renderWidgets(graphics, mouseX, mouseY, partialTick);
        form.renderLabels(graphics, scroll);
        graphics.disableScissor();

        if (maxScroll() > 0) {
            GuiUtil.drawVanillaScrollbar(graphics, scrollbarX(), scrollTrackTop(), scrollTrackHeight(),
                    formViewHeight(), scroll, maxScroll());
        }

        if (!form.isValid()) {
            graphics.drawCenteredString(this.font, Component.translatable("gui.modest_meals.invalid_input"),
                    panelX + PANEL_W / 2, panelY + panelH - FOOTER_H - 6, GuiUtil.ERROR_RED);
        }

        for (DropdownWidget dropdown : form.dropdowns()) {
            dropdown.renderOverlay(graphics, mouseX, mouseY);
        }
    }
}
