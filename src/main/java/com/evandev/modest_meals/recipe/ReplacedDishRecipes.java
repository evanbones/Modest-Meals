package com.evandev.modest_meals.recipe;

import com.evandev.modest_meals.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import java.io.Reader;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ReplacedDishRecipes {

    private static final Set<String> REPLACEABLE_TYPES = Set.of(
            "minecraft:crafting_shaped",
            "minecraft:crafting_shapeless",
            "farmersdelight:cooking"
    );

    private ReplacedDishRecipes() {
    }

    public static Map<ResourceLocation, JsonElement> filter(Map<ResourceLocation, JsonElement> recipes,
                                                            ResourceManager resourceManager) {
        Set<String> replaced = replacedResults(resourceManager);
        if (replaced.isEmpty()) {
            return recipes;
        }

        Map<ResourceLocation, JsonElement> kept = new LinkedHashMap<>();
        int dropped = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : recipes.entrySet()) {
            if (isReplaced(entry.getKey(), entry.getValue(), replaced)) {
                dropped++;
            } else {
                kept.put(entry.getKey(), entry.getValue());
            }
        }
        Constants.LOG.info("Removed {} recipes for dishes now made as meals", dropped);
        return kept;
    }

    private static boolean isReplaced(ResourceLocation id, JsonElement json, Set<String> replaced) {
        if (id.getNamespace().equals(Constants.MOD_ID) || !(json instanceof JsonObject recipe)) {
            return false;
        }
        if (!REPLACEABLE_TYPES.contains(GsonHelper.getAsString(recipe, "type", ""))) {
            return false;
        }
        JsonElement result = recipe.get("result");
        if (result == null) {
            return false;
        }
        if (result.isJsonPrimitive()) {
            return replaced.contains(result.getAsString());
        }
        if (result instanceof JsonObject object) {
            String item = GsonHelper.getAsString(object, "id", GsonHelper.getAsString(object, "item", ""));
            return replaced.contains(item);
        }
        return false;
    }

    private static Set<String> replacedResults(ResourceManager resourceManager) {
        Set<String> results = new HashSet<>();
        Map<ResourceLocation, Resource> files = resourceManager.listResources("modest_meals/meal_types",
                path -> path.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> file : files.entrySet()) {
            try (Reader reader = file.getValue().openAsReader()) {
                JsonObject type = JsonParser.parseReader(reader).getAsJsonObject();
                for (JsonElement element : GsonHelper.getAsJsonArray(type, "item_overrides", new JsonArray())) {
                    JsonObject override = element.getAsJsonObject();
                    if (GsonHelper.getAsBoolean(override, "replace_recipes", false)) {
                        results.add(ResourceLocation.parse(GsonHelper.getAsString(override, "result")).toString());
                    }
                }
            } catch (Exception e) {
                Constants.LOG.error("Failed to read meal type {} for recipe replacement", file.getKey(), e);
            }
        }
        return results;
    }
}
