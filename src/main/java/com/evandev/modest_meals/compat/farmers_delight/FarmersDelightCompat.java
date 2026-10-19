package com.evandev.modest_meals.compat.farmers_delight;

import com.evandev.modest_meals.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class FarmersDelightCompat {
    public static final String MOD_ID = "farmersdelight";

    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Constants.MOD_ID);

    public static Supplier<MealCookingRecipe.Serializer> MEAL_COOKING_SERIALIZER;

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void register(IEventBus modBus) {
        if (!isLoaded()) {
            return;
        }
        MEAL_COOKING_SERIALIZER = RECIPE_SERIALIZERS.register("meal_cooking", MealCookingRecipe.Serializer::new);
        RECIPE_SERIALIZERS.register(modBus);
    }
}
