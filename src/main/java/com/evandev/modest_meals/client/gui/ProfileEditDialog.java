package com.evandev.modest_meals.client.gui;

import com.evandev.modest_meals.client.gui.editor.FormBuilder;
import com.evandev.modest_meals.client.gui.util.GuiUtil;
import com.evandev.modest_meals.client.gui.widget.ModButton;
import com.evandev.modest_meals.food.FoodProfile;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ProfileEditDialog extends Screen {
    private static final int PANEL_W = 288;
    private static final int TITLE_H = 22;
    private static final int HEADER_H = 24;
    private static final int GAP = 8;
    private static final int FOOTER_H = 28;

    private static final int OVERRIDE_PRIORITY = 100;

    private final Screen parent;
    private final Item item;
    private final Consumer<FoodProfile> onSave;

    private float healthPerNutrition;
    private float healthTicksPerPoint;
    private float staminaPerNutrition;

    private FormBuilder form;
    private Button doneButton;
    private int panelX, panelY, panelH;

    public ProfileEditDialog(Screen parent, Item item, FoodProfile existingProfile, Consumer<FoodProfile> onSave) {
        super(Component.translatable("gui.modest_meals.profile_dialog.title"));
        this.parent = parent;
        this.item = item;
        this.onSave = onSave;

        if (existingProfile != null) {
            this.healthPerNutrition = existingProfile.healthPerNutrition();
            this.healthTicksPerPoint = existingProfile.healthTicksPerPoint();
            this.staminaPerNutrition = existingProfile.staminaPerNutrition();
        } else {
            this.healthPerNutrition = 1.0f;
            this.healthTicksPerPoint = 30.0f;
            this.staminaPerNutrition = 0.0f;
        }
    }

    @Override
    protected void init() {
        super.init();

        int contentX0 = GuiUtil.PANEL_PADDING;
        int contentW = PANEL_W - GuiUtil.PANEL_PADDING * 2;
        int formTop = TITLE_H + HEADER_H + GAP;

        int formH = 3 * (FormBuilder.ROW_H + 6) - 6;
        this.panelH = formTop + formH + GAP + FOOTER_H;
        this.panelX = (this.width - PANEL_W) / 2;
        this.panelY = Math.max(4, (this.height - panelH) / 2);

        int contentX = panelX + contentX0;

        this.form = new FormBuilder(this.font, contentX, panelY + formTop, contentW, this::onFormChanged);
        this.form.decimal("gui.modest_meals.field.health_per_nutrition", 0, 100, healthPerNutrition,
                v -> healthPerNutrition = (float) v);
        this.form.decimal("gui.modest_meals.field.health_ticks_per_point", 0, 6000, healthTicksPerPoint,
                v -> healthTicksPerPoint = (float) v);
        this.form.decimal("gui.modest_meals.field.stamina_per_nutrition", 0, 100, staminaPerNutrition,
                v -> staminaPerNutrition = (float) v);
        this.form.widgets().forEach(this::addRenderableWidget);

        int footerY = panelY + panelH - FOOTER_H + 4;
        int buttonW = (contentW - GAP) / 2;
        this.doneButton = this.addRenderableWidget(new ModButton(contentX, footerY, buttonW, 20,
                Component.translatable("gui.done"), b -> save()));
        this.addRenderableWidget(new ModButton(contentX + buttonW + GAP, footerY, buttonW, 20,
                CommonComponents.GUI_CANCEL, b -> this.minecraft.setScreen(this.parent)));

        onFormChanged();
    }

    private void onFormChanged() {
        if (doneButton != null) doneButton.active = form == null || form.isValid();
    }

    private void save() {
        if (form != null && !form.isValid()) return;
        ResourceLocation loc = BuiltInRegistries.ITEM.getKey(item);
        onSave.accept(new FoodProfile(
                loc + "_override",
                loc.toString(),
                OVERRIDE_PRIORITY,
                healthPerNutrition,
                healthTicksPerPoint,
                staminaPerNutrition
        ));
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        GuiUtil.drawDialogPanel(graphics, panelX, panelY, PANEL_W, panelH);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int contentX = panelX + GuiUtil.PANEL_PADDING;
        int contentW = PANEL_W - GuiUtil.PANEL_PADDING * 2;

        graphics.drawCenteredString(this.font, this.title, panelX + PANEL_W / 2, panelY + 8, GuiUtil.WHITE);

        ItemStack stack = new ItemStack(item);
        int headerY = panelY + TITLE_H;
        GuiUtil.drawSlot(graphics, contentX, headerY);
        graphics.renderFakeItem(stack, contentX + 1, headerY + 1);
        GuiUtil.drawTrimmed(graphics, this.font, stack.getHoverName(),
                contentX + 24, headerY + 5, contentW - 24, GuiUtil.WHITE);

        form.renderLabels(graphics, 0);

        if (!form.isValid()) {
            graphics.drawCenteredString(this.font, Component.translatable("gui.modest_meals.invalid_input"),
                    panelX + PANEL_W / 2, panelY + panelH - FOOTER_H - 6, GuiUtil.ERROR_RED);
        }
    }
}
