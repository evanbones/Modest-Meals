package com.evandev.modest_meals.recipe;

import com.evandev.modest_meals.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Constants.MOD_ID);

    public static final Supplier<SimpleCraftingRecipeSerializer<MealCraftingRecipe>> MEAL_CRAFTING =
            RECIPE_SERIALIZERS.register("meal_crafting",
                    () -> new SimpleCraftingRecipeSerializer<>(MealCraftingRecipe::new));

    public static void register(IEventBus modBus) {
        RECIPE_SERIALIZERS.register(modBus);
    }
}
