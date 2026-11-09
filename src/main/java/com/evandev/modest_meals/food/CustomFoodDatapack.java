package com.evandev.modest_meals.food;

import com.evandev.modest_meals.platform.Platform;
import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.ingredient.IngredientProfile;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitType;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
//? if >=26.1 {
/*import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
*///?}
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CustomFoodDatapack {
    private static final String PACK_DESCRIPTION = "Modest Meals Custom Food Datapack";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path PACK_DIR = Platform.getConfigDirectory().resolve(Constants.MOD_ID).resolve("datapack");
    private static final Path TRAITS_FILE = PACK_DIR.resolve("data").resolve(Constants.MOD_ID).resolve("food_traits").resolve("custom_traits.json");
    private static final Path PROFILES_FILE = PACK_DIR.resolve("data").resolve(Constants.MOD_ID).resolve("food_profiles").resolve("custom_profiles.json");
    private static final Path INGREDIENTS_FILE = PACK_DIR.resolve("data").resolve(Constants.MOD_ID)
            .resolve("modest_meals").resolve("ingredients").resolve("custom_ingredients.json");

    public static Path getPackDir() {
        return PACK_DIR;
    }

    public static void ensurePackExists() {
        try {
            if (!Files.exists(PACK_DIR)) {
                Files.createDirectories(PACK_DIR);
            }

            JsonObject root = new JsonObject();
            root.add("pack", packMetadata());
            try (FileWriter writer = new FileWriter(PACK_DIR.resolve("pack.mcmeta").toFile())) {
                GSON.toJson(root, writer);
            }

            if (!Files.exists(TRAITS_FILE.getParent())) {
                Files.createDirectories(TRAITS_FILE.getParent());
            }
            if (!Files.exists(PROFILES_FILE.getParent())) {
                Files.createDirectories(PROFILES_FILE.getParent());
            }
            if (!Files.exists(INGREDIENTS_FILE.getParent())) {
                Files.createDirectories(INGREDIENTS_FILE.getParent());
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to initialize custom food datapack at {}", PACK_DIR, e);
        }
    }

    private static JsonElement packMetadata() {
        //? if >=26.1 {
        /*PackFormat format = SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA);
        return PackMetadataSection.SERVER_TYPE.codec()
                .encodeStart(JsonOps.INSTANCE, new PackMetadataSection(Component.literal(PACK_DESCRIPTION), format.minorRange()))
                .getOrThrow();
        *///?} else {
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA));
        pack.addProperty("description", PACK_DESCRIPTION);
        return pack;
        //?}
    }

    public static Optional<Pack> createPack() {
        ensurePackExists();

        PackLocationInfo info = new PackLocationInfo(
                Constants.MOD_ID + "_custom_data",
                Component.literal("Modest Meals Custom Data"),
                PackSource.DEFAULT,
                Optional.empty()
        );

        Pack.ResourcesSupplier supplier = new PathPackResources.PathResourcesSupplier(PACK_DIR);
        PackSelectionConfig selectionConfig = new PackSelectionConfig(true, Pack.Position.TOP, false);
        return Optional.ofNullable(Pack.readMetaAndCreate(info, supplier, PackType.SERVER_DATA, selectionConfig));
    }

    public static Map<String, List<FoodTrait>> readCustomTraits() {
        Map<String, List<FoodTrait>> map = new LinkedHashMap<>();
        if (!Files.exists(TRAITS_FILE)) {
            return map;
        }

        try (FileReader reader = new FileReader(TRAITS_FILE.toFile())) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json != null && json.has("entries") && json.get("entries").isJsonArray()) {
                for (JsonElement element : json.getAsJsonArray("entries")) {
                    if (element.isJsonObject()) {
                        JsonObject obj = element.getAsJsonObject();
                        if (obj.has("target") && obj.has("traits") && obj.get("traits").isJsonArray()) {
                            String target = obj.get("target").getAsString();
                            List<FoodTrait> list = new ArrayList<>();
                            for (JsonElement t : obj.getAsJsonArray("traits")) {
                                FoodTraitType.CODEC.parse(JsonOps.INSTANCE, t)
                                        .resultOrPartial(err -> Constants.LOG.error("Failed to parse custom trait: {}", err))
                                        .ifPresent(list::add);
                            }
                            if (!list.isEmpty()) {
                                map.put(target, list);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to read custom traits from {}", TRAITS_FILE, e);
        }
        return map;
    }

    public static Map<String, Set<String>> readCustomSuppressions() {
        Map<String, Set<String>> map = new LinkedHashMap<>();
        if (!Files.exists(TRAITS_FILE)) {
            return map;
        }

        try (FileReader reader = new FileReader(TRAITS_FILE.toFile())) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json != null && json.has("entries") && json.get("entries").isJsonArray()) {
                for (JsonElement element : json.getAsJsonArray("entries")) {
                    if (element.isJsonObject()) {
                        JsonObject obj = element.getAsJsonObject();
                        if (obj.has("target") && obj.has("suppress") && obj.get("suppress").isJsonArray()) {
                            Set<String> keys = new LinkedHashSet<>();
                            for (JsonElement key : obj.getAsJsonArray("suppress")) {
                                if (key.isJsonPrimitive()) keys.add(key.getAsString());
                            }
                            if (!keys.isEmpty()) {
                                map.put(obj.get("target").getAsString(), keys);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to read custom trait suppressions from {}", TRAITS_FILE, e);
        }
        return map;
    }

    public static void saveCustomTraits(Map<String, List<FoodTrait>> traitsMap, Map<String, Set<String>> suppressionsMap) {
        ensurePackExists();
        try {
            JsonObject root = new JsonObject();
            JsonArray entries = new JsonArray();

            Set<String> targets = new LinkedHashSet<>(traitsMap.keySet());
            targets.addAll(suppressionsMap.keySet());

            for (String target : targets) {
                List<FoodTrait> traits = traitsMap.get(target);
                Set<String> suppressed = suppressionsMap.get(target);
                boolean hasTraits = traits != null && !traits.isEmpty();
                boolean hasSuppressions = suppressed != null && !suppressed.isEmpty();
                if (!hasTraits && !hasSuppressions) {
                    continue;
                }

                JsonObject entryObj = new JsonObject();
                entryObj.addProperty("target", target);
                if (hasTraits) {
                    JsonArray traitsArr = new JsonArray();
                    for (FoodTrait trait : traits) {
                        FoodTraitType.CODEC.encodeStart(JsonOps.INSTANCE, trait)
                                .resultOrPartial(err -> Constants.LOG.error("Failed to encode custom trait: {}", err))
                                .ifPresent(traitsArr::add);
                    }
                    entryObj.add("traits", traitsArr);
                }
                if (hasSuppressions) {
                    JsonArray suppressArr = new JsonArray();
                    for (String key : suppressed) {
                        suppressArr.add(key);
                    }
                    entryObj.add("suppress", suppressArr);
                }
                entries.add(entryObj);
            }
            root.add("entries", entries);

            try (FileWriter writer = new FileWriter(TRAITS_FILE.toFile())) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to save custom traits to {}", TRAITS_FILE, e);
        }
    }

    public static Map<String, IngredientProfile> readCustomIngredients() {
        Map<String, IngredientProfile> map = new LinkedHashMap<>();
        if (!Files.exists(INGREDIENTS_FILE)) {
            return map;
        }

        try (FileReader reader = new FileReader(INGREDIENTS_FILE.toFile())) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json != null && json.has("entries") && json.get("entries").isJsonArray()) {
                for (JsonElement element : json.getAsJsonArray("entries")) {
                    if (!(element instanceof JsonObject entry) || !entry.has("target")) {
                        continue;
                    }
                    String target = entry.get("target").getAsString();
                    IngredientProfile.CODEC.parse(JsonOps.INSTANCE, entry)
                            .resultOrPartial(err -> Constants.LOG.error(
                                    "Failed to parse custom ingredient {}: {}", target, err))
                            .ifPresent(profile -> map.put(target, profile));
                }
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to read custom ingredients from {}", INGREDIENTS_FILE, e);
        }
        return map;
    }

    public static void saveCustomIngredients(Map<String, IngredientProfile> ingredients) {
        ensurePackExists();
        try {
            JsonArray entries = new JsonArray();
            ingredients.forEach((target, profile) -> IngredientProfile.CODEC
                    .encodeStart(JsonOps.INSTANCE, profile)
                    .resultOrPartial(err -> Constants.LOG.error(
                            "Failed to encode custom ingredient {}: {}", target, err))
                    .ifPresent(encoded -> {
                        JsonObject entry = encoded.getAsJsonObject();
                        entry.addProperty("target", target);
                        entries.add(entry);
                    }));

            JsonObject root = new JsonObject();
            root.add("entries", entries);

            try (FileWriter writer = new FileWriter(INGREDIENTS_FILE.toFile())) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to save custom ingredients to {}", INGREDIENTS_FILE, e);
        }
    }

    public static List<FoodProfile> readCustomProfiles() {
        List<FoodProfile> list = new ArrayList<>();
        if (!Files.exists(PROFILES_FILE)) {
            return list;
        }

        try (FileReader reader = new FileReader(PROFILES_FILE.toFile())) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json != null && json.has("profiles") && json.get("profiles").isJsonArray()) {
                for (JsonElement element : json.getAsJsonArray("profiles")) {
                    FoodProfile.CODEC.parse(JsonOps.INSTANCE, element)
                            .resultOrPartial(err -> Constants.LOG.error("Failed to parse custom profile: {}", err))
                            .ifPresent(list::add);
                }
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to read custom profiles from {}", PROFILES_FILE, e);
        }
        return list;
    }

    public static void saveCustomProfiles(List<FoodProfile> profilesList) {
        ensurePackExists();
        try {
            JsonObject root = new JsonObject();
            JsonArray profilesArr = new JsonArray();

            for (FoodProfile profile : profilesList) {
                FoodProfile.CODEC.encodeStart(JsonOps.INSTANCE, profile)
                        .resultOrPartial(err -> Constants.LOG.error("Failed to encode custom profile: {}", err))
                        .ifPresent(profilesArr::add);
            }
            root.add("profiles", profilesArr);

            try (FileWriter writer = new FileWriter(PROFILES_FILE.toFile())) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to save custom profiles to {}", PROFILES_FILE, e);
        }
    }
}
