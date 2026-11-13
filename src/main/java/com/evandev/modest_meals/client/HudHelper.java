package com.evandev.modest_meals.client;

import com.evandev.modest_meals.config.HudLayoutOption;
import com.evandev.modest_meals.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public abstract class HudHelper {
    public static final ResourceLocation ARMOR_EMPTY_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_empty");
    public static final ResourceLocation ARMOR_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_half");
    public static final ResourceLocation ARMOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_full");
    public static final ResourceLocation AIR_SPRITE = ResourceLocation.withDefaultNamespace("hud/air");
    public static final ResourceLocation AIR_BURSTING_SPRITE = ResourceLocation.withDefaultNamespace("hud/air_bursting");
    private static final int VANILLA_ROW_BASE = 39;

    private static int rowBaseShift;

    public static void captureRowBase(int rowBase) {
        rowBaseShift = rowBase - VANILLA_ROW_BASE;
    }

    public static int rowBaseShift() {
        return rowBaseShift;
    }

    public static boolean isArmorEmpty() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null || player.getArmorValue() <= 0;
    }

    public static int getHeightOffsetFromHearts() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return VANILLA_ROW_BASE + rowBaseShift;
        }

        int absorptionAmount = Mth.ceil(player.getAbsorptionAmount());
        int currentHealth = Mth.ceil(player.getHealth());
        float maxHealthAttr = (float) player.getAttributeValue(Attributes.MAX_HEALTH);
        float maxHealth = Math.max(maxHealthAttr, currentHealth);
        int numberOfRows = Mth.ceil((maxHealth + (float) absorptionAmount) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (numberOfRows - 2), 3);

        return VANILLA_ROW_BASE + rowBaseShift + (numberOfRows - 1) * rowHeight + 10;
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
        RenderCompat.blitSpriteMirrored(graphics, RenderCompat.guiSprite(ARMOR_HALF_SPRITE), x, y, 9, 9);
    }

    public static void renderArmor(GuiGraphics graphics, int offsetHeight, int offsetRight) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        int right = offsetRight;
        int armor = player.getArmorValue();
        int top = graphics.guiHeight() - offsetHeight;

        RenderCompat.enableBlend();

        for (int i = 1; armor > 0 && i < 20; i += 2) {
            right -= 8;

            if (i == armor) {
                renderInverseHalfArmor(graphics, right, top);
            } else if (i < armor) {
                RenderCompat.blitSprite(graphics, ARMOR_FULL_SPRITE, right, top, 9, 9);
            } else {
                RenderCompat.blitSprite(graphics, ARMOR_EMPTY_SPRITE, right, top, 9, 9);
            }
        }

        RenderCompat.disableBlend();
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

        RenderCompat.enableBlend();

        for (int i = 0; i < full + partial; ++i) {
            RenderCompat.blitSprite(graphics, i < full ? AIR_SPRITE : AIR_BURSTING_SPRITE, offsetLeft + i * 8 + 9, top, 9, 9);
        }

        RenderCompat.disableBlend();
    }
}
