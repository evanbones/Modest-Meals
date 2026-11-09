package com.evandev.modest_meals.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

//? if >=26.1 {
/*import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.ARGB;
*///?} else {
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.joml.Matrix4f;
//?}

public final class RenderCompat {
    private static final float RAISED_LAYER_Z = 300.0F;

    public static void prepareHud() {
        //? if <26.1 {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        //?}
    }

    public static void enableBlend() {
        //? if <26.1
        RenderSystem.enableBlend();
    }

    public static void disableBlend() {
        //? if <26.1
        RenderSystem.disableBlend();
    }

    public static TextureAtlasSprite guiSprite(ResourceLocation id) {
        //? if >=26.1 {
        /*return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI).getSprite(id);
        *///?} else
        return Minecraft.getInstance().getGuiSprites().getSprite(id);
    }

    public static TextureAtlasSprite itemSprite(ResourceLocation id) {
        //? if >=26.1 {
        /*return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).getSprite(id);
        *///?} else
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(id);
    }

    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        //? if >=26.1 {
        /*graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
        *///?} else
        graphics.blitSprite(sprite, x, y, width, height);
    }

    public static void blitSprite(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height, int argb) {
        //? if >=26.1 {
        /*graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height, argb);
        *///?} else {
        setColor(graphics, argb);
        graphics.blitSprite(sprite, x, y, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        //?}
    }

    public static void blitSprite(GuiGraphics graphics, TextureAtlasSprite sprite, int x, int y, int width, int height, int argb) {
        //? if >=26.1 {
        /*graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height, argb);
        *///?} else
        graphics.blit(x, y, 0, width, height, sprite, red(argb), green(argb), blue(argb), alpha(argb));
    }

    public static void blitTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v,
                                   int width, int height, int textureWidth, int textureHeight) {
        //? if >=26.1 {
        /*graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
        *///?} else
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    public static void blitTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y, float u, float v,
                                   int width, int height, int textureWidth, int textureHeight, int argb) {
        //? if >=26.1 {
        /*graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight, argb);
        *///?} else {
        setColor(graphics, argb);
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        //?}
    }

    public static void blitRegion(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                                  float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
        //? if >=26.1 {
        /*graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight);
        *///?} else
        graphics.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
    }

    public static void blitSpriteMirrored(GuiGraphics graphics, TextureAtlasSprite sprite, int x, int y, int width, int height) {
        //? if >=26.1 {
        /*graphics.blit(sprite.atlasLocation(), x, y, x + width, y + height, sprite.getU1(), sprite.getU0(), sprite.getV0(), sprite.getV1());
        *///?} else {
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.addVertex(matrix, x, y + height, 0.0F).setUv(sprite.getU1(), sprite.getV1());
        builder.addVertex(matrix, x + width, y + height, 0.0F).setUv(sprite.getU0(), sprite.getV1());
        builder.addVertex(matrix, x + width, y, 0.0F).setUv(sprite.getU0(), sprite.getV0());
        builder.addVertex(matrix, x, y, 0.0F).setUv(sprite.getU1(), sprite.getV0());
        MeshData meshData = builder.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
        //?}
    }

    public static int pixelAbgr(NativeImage image, int x, int y) {
        //? if >=26.1 {
        /*return ARGB.toABGR(image.getPixel(x, y));
        *///?} else
        return image.getPixelRGBA(x, y);
    }

    public static void setPixelAbgr(NativeImage image, int x, int y, int abgr) {
        //? if >=26.1 {
        /*image.setPixelABGR(x, y, abgr);
        *///?} else
        image.setPixelRGBA(x, y, abgr);
    }

    public static DynamicTexture dynamicTexture(ResourceLocation id, NativeImage image) {
        //? if >=26.1 {
        /*return new DynamicTexture(id::toString, image);
        *///?} else
        return new DynamicTexture(image);
    }

    public static void renderWidget(AbstractWidget widget, GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if >=26.1 {
        /*widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
        *///?} else
        widget.render(graphics, mouseX, mouseY, partialTick);
    }

    public static void pushPose(GuiGraphics graphics) {
        //? if >=26.1 {
        /*graphics.pose().pushMatrix();
        *///?} else
        graphics.pose().pushPose();
    }

    public static void popPose(GuiGraphics graphics) {
        //? if >=26.1 {
        /*graphics.pose().popMatrix();
        *///?} else
        graphics.pose().popPose();
    }

    public static void translate(GuiGraphics graphics, float x, float y) {
        //? if >=26.1 {
        /*graphics.pose().translate(x, y);
        *///?} else
        graphics.pose().translate(x, y, 0.0F);
    }

    public static void scale(GuiGraphics graphics, float x, float y) {
        //? if >=26.1 {
        /*graphics.pose().scale(x, y);
        *///?} else
        graphics.pose().scale(x, y, 1.0F);
    }

    public static void raiseLayer(GuiGraphics graphics) {
        //? if >=26.1 {
        /*graphics.nextStratum();
        *///?} else
        graphics.pose().translate(0.0F, 0.0F, RAISED_LAYER_Z);
    }

    //? if <26.1 {
    private static void setColor(GuiGraphics graphics, int argb) {
        graphics.setColor(red(argb), green(argb), blue(argb), alpha(argb));
    }

    private static float alpha(int argb) {
        return (argb >>> 24) / 255.0F;
    }

    private static float red(int argb) {
        return ((argb >> 16) & 0xFF) / 255.0F;
    }

    private static float green(int argb) {
        return ((argb >> 8) & 0xFF) / 255.0F;
    }

    private static float blue(int argb) {
        return (argb & 0xFF) / 255.0F;
    }
    //?}
}
