package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.client.hud.TextureHelper;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    public abstract boolean isGameLoadFinished();

    @Inject(
            method = "onResourceLoadFinished",
            at = @At("RETURN")
    )
    private void mm$onFinishedLoading(@Coerce Object loadingContext, CallbackInfo ci) {
        if (!this.isGameLoadFinished()) {
            return;
        }
        try {
            TextureHelper textureHelper = new TextureHelper();
            textureHelper.generateHudTextures();
        } catch (Exception ignored) {
        }
    }
}
