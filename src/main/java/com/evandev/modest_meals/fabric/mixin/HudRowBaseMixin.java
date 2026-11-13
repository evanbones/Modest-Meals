package com.evandev.modest_meals.fabric.mixin;

//? if fabric {
/*import com.evandev.modest_meals.client.HudHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Gui.class)
public abstract class HudRowBaseMixin {
    //? if >=26.2 {
    /^@Unique
    private static final String ARMOR_TARGET = "Lnet/minecraft/client/gui/Hud;extractArmor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIII)V";
    ^///?} else if >=26.1 {
    /^@Unique
    private static final String ARMOR_TARGET = "Lnet/minecraft/client/gui/Gui;extractArmor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIII)V";
    ^///?} else {
    @Unique
    private static final String ARMOR_TARGET = "Lnet/minecraft/client/gui/Gui;renderArmor(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIII)V";
    //?}

    @ModifyArg(method = "renderPlayerHealth", index = 2, at = @At(value = "INVOKE", target = ARMOR_TARGET))
    private int mm$captureRowBase(GuiGraphics graphics, Player player, int y, int heartRows, int rowHeight, int x) {
        HudHelper.captureRowBase(graphics.guiHeight() - y);
        return y;
    }
}
*///?}
