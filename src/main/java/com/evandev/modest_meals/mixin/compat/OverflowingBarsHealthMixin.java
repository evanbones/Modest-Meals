package com.evandev.modest_meals.mixin.compat;

import com.evandev.modest_meals.compat.overflowing_bars.OverflowingBarsCompat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "fuzs.overflowingbars.client.gui.HealthBarRenderer", remap = false)
public abstract class OverflowingBarsHealthMixin {
    @Unique
    private final RandomSource mm$random = RandomSource.create();

    @Inject(method = "renderHearts", at = @At("TAIL"), require = 0)
    private void mm$drawRestoredHearts(
            GuiGraphics guiGraphics, Player player, int posX, int posY, int heartOffsetByRegen,
            float maxHealth, int currentHealth, int displayHealth, int currentAbsorptionHealth, boolean blink,
            CallbackInfo ci
    ) {
        OverflowingBarsCompat.renderRestoredHearts(guiGraphics, player, mm$random, posX, posY, maxHealth, currentHealth);
    }
}
