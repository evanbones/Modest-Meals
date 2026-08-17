package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.compat.farmers_delight.NourishmentEffectHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MobEffectTextureManager.class)
public abstract class MobEffectTextureManagerMixin {

    @WrapMethod(method = "get")
    private TextureAtlasSprite mm$substituteEffectSprite(Holder<MobEffect> effect, Operation<TextureAtlasSprite> original) {
        return original.call(NourishmentEffectHandler.getEffectForSprite(effect));
    }
}
