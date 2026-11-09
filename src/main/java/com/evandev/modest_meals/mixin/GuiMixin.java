package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.client.RenderCompat;
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
    //? if >=26.2 {
    /*@Unique
    private static final String HEART_TARGET = "Lnet/minecraft/client/gui/Hud;extractHeart(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Hud$HeartType;IIZZZ)V";
    *///?} else if >=26.1 {
    /*@Unique
    private static final String HEART_TARGET = "Lnet/minecraft/client/gui/Gui;extractHeart(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Gui$HeartType;IIZZZ)V";
    *///?} else {
    @Unique
    private static final String HEART_TARGET = "Lnet/minecraft/client/gui/Gui;renderHeart(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Gui$HeartType;IIZZZ)V";
    //?}
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
        int color = renderedHeart.color().getRGB();
        if (renderedHeart.isAtlasTexture()) {
            RenderCompat.blitSprite(context, RenderCompat.guiSprite(renderedHeart.texture()), x, y, 9, 9, color);
            return;
        }
        RenderCompat.blitTexture(context, renderedHeart.texture(), x, y, 0.0F, 0.0F, 9, 9, 9, 9, color);
    }

    @WrapOperation(
            method = "renderHearts",
            at = @At(
                    value = "INVOKE",
                    target = HEART_TARGET
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
