package com.evandev.modest_meals.datagen;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.compat.cultural_delights.CulturalDelightsCompat;
import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightCompat;
import com.evandev.modest_meals.registry.ModItems;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MealRecipeProvider implements DataProvider {

    private static final List<String> POT_MEAL_TYPES = List.of("soup", "curry", "jam", "ramen", "pasta", "porridge");
    private static final List<String> COLD_VAT_MEAL_TYPES = List.of("ice_cream");

    private final PackOutput.PathProvider pathProvider;

    public MealRecipeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    private static JsonArray modLoaded(String modId) {
        JsonObject condition = new JsonObject();
        condition.addProperty("type", "neoforge:mod_loaded");
        condition.addProperty("modid", modId);
        JsonArray conditions = new JsonArray();
        conditions.add(condition);
        return conditions;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        JsonObject crafting = new JsonObject();
        crafting.addProperty("type", Constants.MOD_ID + ":meal_crafting");
        futures.add(write(output, "meal_crafting", crafting));

        for (String mealType : POT_MEAL_TYPES) {
            JsonObject cooking = new JsonObject();
            cooking.add("neoforge:conditions", modLoaded(FarmersDelightCompat.MOD_ID));
            cooking.addProperty("type", Constants.MOD_ID + ":meal_cooking");
            cooking.addProperty("meal_type", Constants.MOD_ID + ":" + mealType);
            cooking.addProperty("cookingtime", 200);
            futures.add(write(output, "cooking/" + mealType, cooking));
        }

        ModItems.PORTIONS.forEach((portion, info) -> {
            String portionId = BuiltInRegistries.ITEM.getKey(portion.get()).toString();
            JsonObject cutting = new JsonObject();
            cutting.add("neoforge:conditions", modLoaded(FarmersDelightCompat.MOD_ID));
            cutting.addProperty("type", Constants.MOD_ID + ":meal_cutting");
            cutting.addProperty("meal", BuiltInRegistries.ITEM.getKey(info.whole().get()).toString());
            cutting.addProperty("portion", portionId);
            cutting.addProperty("count", info.count());
            futures.add(write(output, "cutting/" + BuiltInRegistries.ITEM.getKey(portion.get()).getPath(), cutting));
        });

        for (String mealType : COLD_VAT_MEAL_TYPES) {
            JsonObject chilling = new JsonObject();
            chilling.add("neoforge:conditions", modLoaded(CulturalDelightsCompat.MOD_ID));
            chilling.addProperty("type", Constants.MOD_ID + ":meal_vat");
            chilling.addProperty("meal_type", Constants.MOD_ID + ":" + mealType);
            chilling.addProperty("temperature", "cold");
            chilling.addProperty("cooktime", 200);
            futures.add(write(output, "chilling/" + mealType, chilling));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> write(CachedOutput output, String path, JsonElement json) {
        Path file = pathProvider.json(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path));
        return DataProvider.saveStable(output, json, file);
    }

    @Override
    public String getName() {
        return "Modest Meals recipes";
    }
}
