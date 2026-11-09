package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.food.ItemTagLookup;
import com.evandev.modest_meals.recipe.ReplacedDishRecipes;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

//? if >=26.1 {
/*import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.Recipe;
import java.util.stream.Stream;
*///?} else {
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
//?}

//? if >=26.3 {
/*@Mixin(RecipeMap.class)
public abstract class RecipeManagerMixin {
    @WrapOperation(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/HolderLookup;listElements()Ljava/util/stream/Stream;"))
    private static Stream<Holder.Reference<Recipe<?>>> modest_meals$dropReplacedDishes(HolderLookup<Recipe<?>> lookup,
                                                                                        Operation<Stream<Holder.Reference<Recipe<?>>>> original) {
        return ReplacedDishRecipes.withoutReplaced(original.call(lookup));
    }
}
*///?} else if >=26.1 {
/*@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {
    @WrapOperation(method = "prepare", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/crafting/RecipeMap;create(Ljava/lang/Iterable;)Lnet/minecraft/world/item/crafting/RecipeMap;"))
    private RecipeMap modest_meals$dropReplacedDishes(Iterable<RecipeHolder<?>> recipes, Operation<RecipeMap> original,
                                                      @Local(argsOnly = true) ResourceManager resourceManager) {
        return original.call(ReplacedDishRecipes.withoutReplaced(recipes, resourceManager, ItemTagLookup.UNKNOWN));
    }
}
*///?} else {
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin extends SimpleJsonResourceReloadListener {

    private RecipeManagerMixin() {
        super(null, "");
    }

    @WrapMethod(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
    private void modest_meals$dropReplacedDishes(Map<ResourceLocation, JsonElement> recipes, ResourceManager resourceManager,
                                                  ProfilerFiller profiler, Operation<Void> original) {
        original.call(ReplacedDishRecipes.filter(recipes, resourceManager, mm$tagLookup()), resourceManager, profiler);
    }

    @org.spongepowered.asm.mixin.Unique
    private ItemTagLookup mm$tagLookup() {
        //? if neoforge {
        /*return ItemTagLookup.of(this.getContext());
        *///?} else
        return ItemTagLookup.UNKNOWN;
    }
}
//?}
