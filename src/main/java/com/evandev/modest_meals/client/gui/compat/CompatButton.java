package com.evandev.modest_meals.client.gui.compat;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}

public abstract class CompatButton extends Button {
    protected CompatButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    //? if >=26.1 {
    /*@Override
    protected final void extractContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    protected abstract void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    protected boolean clicked(double mouseX, double mouseY) {
        return this.isActive() && this.isMouseOver(mouseX, mouseY);
    }
    *///?}
}
