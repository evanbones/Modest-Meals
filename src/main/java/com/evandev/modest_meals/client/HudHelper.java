package com.evandev.modest_meals.client;

import com.evandev.modest_meals.config.HudLayoutOption;
import com.evandev.modest_meals.config.ModConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

public abstract class HudHelper {
    public static final ResourceLocation ARMOR_EMPTY_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_empty");
    public static final ResourceLocation ARMOR_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_half");
    public static final ResourceLocation ARMOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_full");
    public static final ResourceLocation AIR_SPRITE = ResourceLocation.withDefaultNamespace("hud/air");
    public static final ResourceLocation AIR_BURSTING_SPRITE = ResourceLocation.withDefaultNamespace("hud/air_bursting");

    public static boolean isArmorEmpty() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null || player.getArmorValue() <= 0;
    }

    public static int getHeightOffsetFromHearts() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return 39;
        }

        int absorptionAmount = Mth.ceil(player.getAbsorptionAmount());
        int currentHealth = Mth.ceil(player.getHealth());
        float maxHealthAttr = (float) player.getAttributeValue(Attributes.MAX_HEALTH);
        float maxHealth = Math.max(maxHealthAttr, currentHealth);
        int numberOfRows = Mth.ceil((maxHealth + (float) absorptionAmount) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (numberOfRows - 2), 3);

        return 39 + (numberOfRows - 1) * rowHeight + 10;
    }

    public static int getHeightOffsetForStamina() {
        int heightOffset = 49;
        boolean isFoodOff = ModConfig.get().hideHungerBar;
        HudLayoutOption layout = ModConfig.get().hudLayout;
        LocalPlayer player = Minecraft.getInstance().player;

        if (isFoodOff) {
            if (layout == HudLayoutOption.CLASSIC) {
                int armorValue = player != null ? player.getArmorValue() : 0;
                if (armorValue == 0) {
                    heightOffset -= 10;
                }
            } else {
                heightOffset -= 10;
            }
        }

        return heightOffset;
    }

    private static void renderInverseHalfArmor(GuiGraphics graphics, int x, int y) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getGuiSprites().getSprite(ARMOR_HALF_SPRITE);

        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Matrix4f matrix = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        builder.addVertex(matrix, x, y + 9, 0.0F).setUv(sprite.getU1(), sprite.getV1());
        builder.addVertex(matrix, x + 9, y + 9, 0.0F).setUv(sprite.getU0(), sprite.getV1());
        builder.addVertex(matrix, x + 9, y, 0.0F).setUv(sprite.getU0(), sprite.getV0());
        builder.addVertex(matrix, x, y, 0.0F).setUv(sprite.getU1(), sprite.getV0());

        MeshData meshData = builder.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
    }

    public static void renderArmor(GuiGraphics graphics, int offsetHeight, int offsetRight) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        int right = offsetRight;
        int armor = player.getArmorValue();
        int top = graphics.guiHeight() - offsetHeight;

        RenderSystem.enableBlend();

        for (int i = 1; armor > 0 && i < 20; i += 2) {
            right -= 8;

            if (i == armor) {
                renderInverseHalfArmor(graphics, right, top);
            } else if (i < armor) {
                graphics.blitSprite(ARMOR_FULL_SPRITE, right, top, 9, 9);
            } else {
                graphics.blitSprite(ARMOR_EMPTY_SPRITE, right, top, 9, 9);
            }
        }

        RenderSystem.disableBlend();
    }

    public static void renderAir(GuiGraphics graphics, int offsetHeight, int offsetLeft) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        int air = player.getAirSupply();
        int top = graphics.guiHeight() - offsetHeight;

        int full = Mth.ceil((double) (air - 2) * 10.0D / 300.0D);
        int partial = Mth.ceil((double) air * 10.0D / 300.0D) - full;

        RenderSystem.enableBlend();

        for (int i = 0; i < full + partial; ++i) {
            graphics.blitSprite(i < full ? AIR_SPRITE : AIR_BURSTING_SPRITE, offsetLeft + i * 8 + 9, top, 9, 9);
        }

        RenderSystem.disableBlend();
    }
}
