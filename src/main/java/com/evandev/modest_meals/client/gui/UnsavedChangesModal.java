package com.evandev.modest_meals.client.gui;

import com.evandev.modest_meals.client.gui.util.GuiUtil;
import com.evandev.modest_meals.client.gui.widget.ModButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UnsavedChangesModal extends Screen {
    private static final int MODAL_W = 260;
    private static final int MODAL_H = 110;

    private final Screen parent;
    private final Runnable onSave;
    private final Runnable onDiscard;

    public UnsavedChangesModal(Screen parent, Runnable onSave, Runnable onDiscard) {
        super(Component.translatable("gui.modest_meals.unsaved.title"));
        this.parent = parent;
        this.onSave = onSave;
        this.onDiscard = onDiscard;
    }

    @Override
    protected void init() {
        int btnY = this.height / 2 - MODAL_H / 2 + MODAL_H - 28;
        int btnW = 78;
        int gap = 4;
        int x = this.width / 2 - (btnW * 3 + gap * 2) / 2;

        this.addRenderableWidget(new ModButton(x, btnY, btnW, 20,
                Component.translatable("gui.modest_meals.unsaved.save"), b -> onSave.run()));
        this.addRenderableWidget(new ModButton(x + btnW + gap, btnY, btnW, 20,
                Component.translatable("gui.modest_meals.unsaved.discard"), b -> onDiscard.run()));
        this.addRenderableWidget(new ModButton(x + (btnW + gap) * 2, btnY, btnW, 20,
                CommonComponents.GUI_CANCEL, b -> this.minecraft.setScreen(parent)));
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int modalY = this.height / 2 - MODAL_H / 2;
        GuiUtil.drawDialogPanel(graphics, centerX - MODAL_W / 2, modalY, MODAL_W, MODAL_H);

        graphics.drawCenteredString(this.font, this.title, centerX, modalY + 12, GuiUtil.WHITE);

        List<FormattedCharSequence> lines =
                this.font.split(Component.translatable("gui.modest_meals.unsaved.message"), MODAL_W - 24);
        int lineY = modalY + 34;
        for (FormattedCharSequence line : lines) {
            graphics.drawCenteredString(this.font, line, centerX, lineY, GuiUtil.LABEL);
            lineY += 11;
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }
}
