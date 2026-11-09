package com.evandev.modest_meals.client.gui.compat;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
*///?}

public abstract class CompatWidget extends AbstractWidget {
    protected CompatWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    //? if >=26.1 {
    /*@Override
    protected final void extractWidgetRenderState(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderWidget(graphics, mouseX, mouseY, partialTick);
    }

    protected abstract void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    protected boolean clicked(double mouseX, double mouseY) {
        return this.isActive() && this.isMouseOver(mouseX, mouseY);
    }

    @Override
    public final boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return mouseClicked(event.x(), event.y(), event.button());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(GuiCompat.mouseEvent(mouseX, mouseY, button), false);
    }

    @Override
    public final boolean keyPressed(KeyEvent event) {
        return keyPressed(event.key(), event.scancode(), event.modifiers());
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
    }

    @Override
    public final boolean charTyped(CharacterEvent event) {
        return charTyped((char) event.codepoint(), 0);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(new CharacterEvent(codePoint));
    }
    *///?}
}
