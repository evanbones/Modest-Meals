package com.evandev.modest_meals.fabric.mixin;

//? if fabric && <26.1 {
/*import com.evandev.modest_meals.client.HudHelper;
import com.evandev.modest_meals.client.HudLayers;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Unique
    private static final int mm$FOOD_ROW_HEIGHT = 39;
    @Unique
    private static final int mm$AIR_ROW_HEIGHT = 49;

    @Unique
    private boolean mm$foodRendered;
    @Unique
    private int mm$airShift;

    @Inject(method = "renderPlayerHealth", at = @At("HEAD"))
    private void mm$resetRightStack(GuiGraphics graphics, CallbackInfo ci) {
        this.mm$foodRendered = false;
        this.mm$airShift = 0;
    }

    @WrapWithCondition(method = "renderPlayerHealth", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;renderArmor(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIII)V"))
    private static boolean mm$hideArmor(GuiGraphics graphics, Player player, int y, int heartRows, int height, int x) {
        return !HudLayers.hidesVanillaArmorAndAir();
    }

    @WrapWithCondition(method = "renderPlayerHealth", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V"))
    private boolean mm$hideFood(Gui gui, GuiGraphics graphics, Player player, int y, int x) {
        this.mm$foodRendered = !HudLayers.hidesVanillaFood();
        return this.mm$foodRendered;
    }

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE_STRING",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=air"))
    private void mm$renderRightStack(GuiGraphics graphics, CallbackInfo ci) {
        int rowBaseShift = HudHelper.rowBaseShift();
        int rightHeight = (this.mm$foodRendered ? mm$AIR_ROW_HEIGHT : mm$FOOD_ROW_HEIGHT) + rowBaseShift;
        rightHeight = HudLayers.renderClassicArmor(graphics, rightHeight);
        rightHeight = HudLayers.renderClassicStamina(graphics, rightHeight);
        rightHeight = HudLayers.renderDefaultStamina(graphics, rightHeight);
        this.mm$airShift = rightHeight - mm$AIR_ROW_HEIGHT - rowBaseShift;
        HudLayers.renderClassicAir(graphics);
    }

    @WrapWithCondition(method = "renderPlayerHealth", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private boolean mm$hideAir(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height) {
        return !HudLayers.hidesVanillaArmorAndAir();
    }

    @ModifyArg(method = "renderPlayerHealth", index = 2, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private int mm$shiftAir(int y) {
        return y - this.mm$airShift;
    }
}
*///?}
