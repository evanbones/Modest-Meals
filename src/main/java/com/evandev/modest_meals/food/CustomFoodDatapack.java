package com.evandev.modest_meals.food;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitType;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CustomFoodDatapack {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path PACK_DIR = FMLPaths.CONFIGDIR.get().resolve(Constants.MOD_ID).resolve("datapack");
    private static final Path TRAITS_FILE = PACK_DIR.resolve("data").resolve(Constants.MOD_ID).resolve("food_traits").resolve("custom_traits.json");
    private static final Path PROFILES_FILE = PACK_DIR.resolve("data").resolve(Constants.MOD_ID).resolve("food_profiles").resolve("custom_profiles.json");

    public static Path getPackDir() {
        return PACK_DIR;
    }

    public static void ensurePackExists() {
        try {
            if (!Files.exists(PACK_DIR)) {
                Files.createDirectories(PACK_DIR);
            }

            Path mcmeta = PACK_DIR.resolve("pack.mcmeta");
            if (!Files.exists(mcmeta)) {
                JsonObject root = new JsonObject();
                JsonObject packObj = new JsonObject();
                packObj.addProperty("pack_format", 48);
                packObj.addProperty("description", "Modest Meals Custom Food Datapack");
                root.add("pack", packObj);

                try (FileWriter writer = new FileWriter(mcmeta.toFile())) {
                    GSON.toJson(root, writer);
                }
            }

            if (!Files.exists(TRAITS_FILE.getParent())) {
                Files.createDirectories(TRAITS_FILE.getParent());
            }
            if (!Files.exists(PROFILES_FILE.getParent())) {
                Files.createDirectories(PROFILES_FILE.getParent());
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to initialize custom food datapack at {}", PACK_DIR, e);
        }
    }

    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            ensurePackExists();

            PackLocationInfo info = new PackLocationInfo(
                    Constants.MOD_ID + "_custom_data",
                    Component.literal("Modest Meals Custom Data"),
                    PackSource.DEFAULT,
                    Optional.empty()
            );

            Pack.ResourcesSupplier supplier = new PathPackResources.PathResourcesSupplier(PACK_DIR);
            PackSelectionConfig selectionConfig = new PackSelectionConfig(true, Pack.Position.TOP, false);
            Pack pack = Pack.readMetaAndCreate(info, supplier, PackType.SERVER_DATA, selectionConfig);

            if (pack != null) {
                event.addRepositorySource(packConsumer -> packConsumer.accept(pack));
            }
        }
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

    public static void saveCustomTraits(Map<String, List<FoodTrait>> traitsMap) {
        ensurePackExists();
        try {
            JsonObject root = new JsonObject();
            JsonArray entries = new JsonArray();

            for (Map.Entry<String, List<FoodTrait>> entry : traitsMap.entrySet()) {
                if (entry.getValue() == null || entry.getValue().isEmpty()) {
                    continue;
                }
                JsonObject entryObj = new JsonObject();
                entryObj.addProperty("target", entry.getKey());
                JsonArray traitsArr = new JsonArray();
                for (FoodTrait trait : entry.getValue()) {
                    FoodTraitType.CODEC.encodeStart(JsonOps.INSTANCE, trait)
                            .resultOrPartial(err -> Constants.LOG.error("Failed to encode custom trait: {}", err))
                            .ifPresent(traitsArr::add);
                }
                entryObj.add("traits", traitsArr);
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
