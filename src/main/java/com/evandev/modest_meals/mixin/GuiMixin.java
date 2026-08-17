package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.client.hud.RestoredHeartsDrawHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private RandomSource random;

    @Unique
    private RestoredHeartsDrawHelper mm$restoredHeartsDrawHelper = null;

    @Shadow
    @Nullable
    protected abstract Player getCameraPlayer();

    @Inject(method = "renderHearts", at = @At("HEAD"))
    private void mm$prepareRestoredHeartsHelper(
            GuiGraphics guiGraphics, Player player, int x, int y, int lines,
            int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking,
            CallbackInfo ci
    ) {
        Player cameraPlayer = this.getCameraPlayer();
        if (cameraPlayer != null) {
            this.mm$restoredHeartsDrawHelper = new RestoredHeartsDrawHelper(cameraPlayer, this.random);
        }
    }

    @Unique
    private void mm$drawHeartWithColor(
            GuiGraphics context, RestoredHeartsDrawHelper.RenderedHeart renderedHeart, int x, int y
    ) {
        float[] colorComponents = renderedHeart.color().getRGBComponents(null);
        if (renderedHeart.isAtlasTexture()) {
            TextureAtlasSprite sprite = minecraft.getGuiSprites().getSprite(renderedHeart.texture());
            context.blit(x, y, 0, 9, 9, sprite, colorComponents[0], colorComponents[1], colorComponents[2], colorComponents[3]);
            return;
        }
        context.setColor(colorComponents[0], colorComponents[1], colorComponents[2], colorComponents[3]);
        context.blit(renderedHeart.texture(), x, y, 0.0F, 0.0F, 9, 9, 9, 9);
        context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Draws amount of hearts that can be restored by eating currently held food item or that are regenerating
     */
    @WrapOperation(
            method = "renderHearts",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIZZZ)V"
            )
    )
    private void mm$drawRestoredHearts(
            Gui inGameHud, GuiGraphics drawContext, Gui.HeartType type, int x, int y, boolean hardcore,
            boolean blinking, boolean half, Operation<Void> original
    ) {
        if (mm$restoredHeartsDrawHelper == null) {
            original.call(inGameHud, drawContext, type, x, y, hardcore, blinking, half);
            return;
        }

        if (type != Gui.HeartType.CONTAINER) {
            original.call(
                    inGameHud, drawContext, type, x, mm$restoredHeartsDrawHelper.getCurrentY(), hardcore, blinking, half
            );
            return;
        }

        y = mm$restoredHeartsDrawHelper.updateCurrentY(y);
        var res = mm$restoredHeartsDrawHelper.heartsToDraw();
        RestoredHeartsDrawHelper.RenderedHeart firstHeart = res.getFirst();
        if (firstHeart != null) {
            // drawing container for correct background
            original.call(inGameHud, drawContext, Gui.HeartType.CONTAINER, x, y, hardcore, blinking, half);
            mm$drawHeartWithColor(drawContext, firstHeart, x, y);
            RestoredHeartsDrawHelper.RenderedHeart secondHeart = res.getSecond();
            if (secondHeart != null) {
                // drawing second heart on top of the first
                mm$drawHeartWithColor(drawContext, secondHeart, x, y);
            }
        } else {
            original.call(inGameHud, drawContext, type, x, y, hardcore, blinking, half);
        }
        mm$restoredHeartsDrawHelper.updateCurrentHeart();
    }
}
