package com.evandev.modest_meals.client.gui.compat;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
*///?}

public abstract class CompatEditBox extends EditBox {
    protected CompatEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    //? if >=26.1 {
    /*private java.util.function.Predicate<String> filter = value -> true;

    public void setFilter(java.util.function.Predicate<String> filter) {
        this.filter = filter;
    }

    @Override
    public void insertText(String input) {
        String previous = this.getValue();
        super.insertText(input);
        if (!this.filter.test(this.getValue())) {
            this.setValue(previous);
        }
    }
    *///?}

    //? if >=26.1 {
    /*@Override
    public final void extractWidgetRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public final void onClick(MouseButtonEvent event, boolean doubleClick) {
        onClick(event.x(), event.y());
    }

    public void onClick(double mouseX, double mouseY) {
        super.onClick(GuiCompat.mouseEvent(mouseX, mouseY, 0), false);
    }

    @Override
    public final boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return mouseClicked(event.x(), event.y(), event.button());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(GuiCompat.mouseEvent(mouseX, mouseY, button), false);
    }
    *///?}
}
