package com.evandev.modest_meals.mixin;

import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;

//? if >=26.3 {
/*import com.evandev.modest_meals.recipe.ReplacedDishRecipes;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
*///?}

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {
    //? if >=26.3 {
    /*@Inject(method = "loadResources", at = @At("HEAD"))
    private static void mm$captureResources(CallbackInfoReturnable<?> cir, @Local(argsOnly = true) ResourceManager resourceManager) {
        ReplacedDishRecipes.setLoadingResources(resourceManager);
    }
    *///?}
}
