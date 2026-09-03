package com.evandev.modest_meals.client.gui.widget;

import com.evandev.modest_meals.client.gui.util.GuiUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ModButton extends Button {
    private static final ResourceLocation SPRITE = ResourceLocation.withDefaultNamespace("widget/button");
    private static final ResourceLocation SPRITE_HOVERED = ResourceLocation.withDefaultNamespace("widget/button_highlighted");
    private static final ResourceLocation SPRITE_DISABLED = ResourceLocation.withDefaultNamespace("widget/button_disabled");

    public ModButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        ResourceLocation sprite = !this.active ? SPRITE_DISABLED : (this.isHovered() ? SPRITE_HOVERED : SPRITE);
        RenderSystem.enableBlend();
        guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        RenderSystem.disableBlend();

        var font = Minecraft.getInstance().font;
        String text = GuiUtil.trim(font, this.getMessage().getString(), this.getWidth() - 6);
        int color = this.active ? GuiUtil.WHITE : 0xA0A0A0;
        guiGraphics.drawCenteredString(font, text,
                this.getX() + this.getWidth() / 2,
                this.getY() + (this.getHeight() - 8) / 2,
                color | 0xFF000000);
    }
}
