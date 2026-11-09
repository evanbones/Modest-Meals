package com.evandev.modest_meals.client.gui.widget;

import com.evandev.modest_meals.client.RenderCompat;
import com.evandev.modest_meals.client.gui.compat.CompatEditBox;
import com.evandev.modest_meals.client.gui.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ModEditBox extends CompatEditBox {
    private static final int TEXT_INSET_X = 4;
    private static final int PLACEHOLDER_COLOR = 0xFF808080;

    private Component placeholder;

    public ModEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        this.setBordered(false);
    }

    public void setPlaceholder(Component placeholder) {
        this.placeholder = placeholder;
    }

    private int textInsetY() {
        return (this.height - 8) / 2;
    }

    @Override
    public int getInnerWidth() {
        return this.width - TEXT_INSET_X * 2;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX - TEXT_INSET_X, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && isMouseOver(mouseX, mouseY)) {
            this.setValue("");
            this.setFocused(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (!this.isVisible()) return;

        ResourceLocation sprite = this.isActive() && this.isFocused() ? GuiUtil.TEXT_FIELD_HIGHLIGHTED : GuiUtil.TEXT_FIELD;
        RenderCompat.blitSprite(guiGraphics, sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        RenderCompat.pushPose(guiGraphics);
        RenderCompat.translate(guiGraphics, TEXT_INSET_X, textInsetY());
        if (placeholder != null && this.getValue().isEmpty() && !this.isFocused()) {
            guiGraphics.drawString(Minecraft.getInstance().font, placeholder, this.getX(), this.getY(), PLACEHOLDER_COLOR);
        }
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        RenderCompat.popPose(guiGraphics);
    }
}
