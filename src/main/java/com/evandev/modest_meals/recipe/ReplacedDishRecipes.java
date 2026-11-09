package com.evandev.modest_meals.recipe;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.compat.reliable_remover.ReliableRemoverCompat;
import com.evandev.modest_meals.food.meal.MealType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
//? if >=26.1 {
/*import com.evandev.modest_meals.mixin.ShapedRecipeAccessor;
import com.evandev.modest_meals.mixin.ShapelessRecipeAccessor;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import java.util.stream.Stream;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
*///?}
import com.evandev.modest_meals.food.ItemTagLookup;

import java.io.Reader;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ReplacedDishRecipes {

    private static final Set<String> REPLACEABLE_TYPES = Set.of(
            "minecraft:crafting_shaped",
            "minecraft:crafting_shapeless",
            "farmersdelight:cooking",
            "culturaldelights:aging"
    );

    public static Map<ResourceLocation, JsonElement> filter(Map<ResourceLocation, JsonElement> recipes,
                                                            ResourceManager resourceManager, ItemTagLookup context) {
        Set<String> replaced = replacedResults(resourceManager, context);
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

    //? if >=26.1 {
    /*public static List<RecipeHolder<?>> withoutReplaced(Iterable<RecipeHolder<?>> recipes, ResourceManager resourceManager,
                                                         ItemTagLookup context) {
        Set<String> replaced = replacedResults(resourceManager, context);
        List<RecipeHolder<?>> kept = new ArrayList<>();
        int dropped = 0;
        for (RecipeHolder<?> holder : recipes) {
            if (!replaced.isEmpty() && isReplaced(holder, replaced)) {
                dropped++;
            } else {
                kept.add(holder);
            }
        }
        if (dropped > 0) {
            Constants.LOG.info("Removed {} recipes for dishes now made as meals", dropped);
        }
        return kept;
    }

    private static boolean isReplaced(RecipeHolder<?> holder, Set<String> replaced) {
        return isReplaced(holder.id(), holder.value(), replaced);
    }

    private static boolean isReplaced(ResourceKey<Recipe<?>> id, Recipe<?> recipe, Set<String> replaced) {
        if (id.identifier().getNamespace().equals(Constants.MOD_ID)) {
            return false;
        }
        ItemStackTemplate result = switch (recipe) {
            case ShapedRecipe shaped -> ((ShapedRecipeAccessor) shaped).mm$result();
            case ShapelessRecipe shapeless -> ((ShapelessRecipeAccessor) shapeless).mm$result();
            default -> null;
        };
        return result != null && result.item().unwrapKey()
                .map(key -> replaced.contains(key.location().toString()))
                .orElse(false);
    }

    private static volatile ResourceManager loadingResources;

    public static void setLoadingResources(ResourceManager resourceManager) {
        loadingResources = resourceManager;
    }

    public static Stream<Holder.Reference<Recipe<?>>> withoutReplaced(Stream<Holder.Reference<Recipe<?>>> recipes) {
        ResourceManager resourceManager = loadingResources;
        if (resourceManager == null) {
            return recipes;
        }
        Set<String> replaced = replacedResults(resourceManager, ItemTagLookup.UNKNOWN);
        if (replaced.isEmpty()) {
            return recipes;
        }
        return recipes.filter(holder -> !isReplaced(holder.key(), holder.value(), replaced));
    }
    *///?}

    private static boolean isReplaced(ResourceLocation id, JsonElement json, Set<String> replaced) {
        if (id.getNamespace().equals(Constants.MOD_ID) || !(json instanceof JsonObject recipe)) {
            return false;
        }
        ResourceLocation type = ResourceLocation.tryParse(GsonHelper.getAsString(recipe, "type", ""));
        if (type == null || !REPLACEABLE_TYPES.contains(type.toString())) {
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

    private static Set<String> replacedResults(ResourceManager resourceManager, ItemTagLookup context) {
        Set<String> results = new HashSet<>();
        Map<ResourceLocation, Resource> files = resourceManager.listResources("modest_meals/meal_types",
                path -> path.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> file : files.entrySet()) {
            try (Reader reader = file.getValue().openAsReader()) {
                JsonObject type = JsonParser.parseReader(reader).getAsJsonObject();
                if (!MealType.Station.byName(GsonHelper.getAsString(type, "station", "crafting_table")).isAvailable()) {
                    continue;
                }
                for (JsonElement element : GsonHelper.getAsJsonArray(type, "item_overrides", new JsonArray())) {
                    JsonObject override = element.getAsJsonObject();
                    if (GsonHelper.getAsBoolean(override, "replace_recipes", false)) {
                        String result = ResourceLocation.parse(GsonHelper.getAsString(override, "result")).toString();
                        if (ReliableRemoverCompat.isSatisfiable(result, strings(override, "ingredients"), strings(override, "base"), context)) {
                            results.add(result);
                        }
                    }
                }
            } catch (Exception e) {
                Constants.LOG.error("Failed to read meal type {} for recipe replacement", file.getKey(), e);
            }
        }
        return results;
    }

    private static List<String> strings(JsonObject object, String key) {
        List<String> out = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(object, key, new JsonArray())) {
            out.add(element.getAsString());
        }
        return out;
    }
}
