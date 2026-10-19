package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.recipe.ReplacedDishRecipes;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @WrapMethod(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
    private void modest_meals$dropReplacedDishes(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager,
                                                  ProfilerFiller profiler, Operation<Void> original) {
        original.call(ReplacedDishRecipes.filter(recipes, resourceManager), resourceManager, profiler);
    }
}
