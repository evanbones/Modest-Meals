package com.evandev.modest_meals.food.meal;

import com.evandev.modest_meals.Constants;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads {@link MealType}s from {@code data/<namespace>/modest_meals/meal_types/}.
 */
public class MealTypeManager extends SimpleJsonResourceReloadListener {
    public static final MealTypeManager INSTANCE = new MealTypeManager();

    private final Map<ResourceLocation, MealType> types = new ConcurrentHashMap<>();
    private volatile List<MealType> craftingTableTypes = List.of();
    private volatile List<MealType> cookingPotTypes = List.of();

    public MealTypeManager() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "modest_meals/meal_types");
    }

    public static Optional<MealType> get(ResourceLocation id) {
        return Optional.ofNullable(INSTANCE.types.get(id));
    }

    public static Collection<MealType> all() {
        return List.copyOf(INSTANCE.types.values());
    }

    public static List<MealType> craftingTable() {
        return INSTANCE.craftingTableTypes;
    }

    public static List<MealType> cookingPot() {
        return INSTANCE.cookingPotTypes;
    }

    public static Map<ResourceLocation, MealType> snapshot() {
        return Map.copyOf(INSTANCE.types);
    }

    public static void applyFromNetwork(Map<ResourceLocation, MealType> loaded) {
        INSTANCE.types.clear();
        INSTANCE.types.putAll(loaded);
        INSTANCE.reindex();
    }

    private void reindex() {
        Comparator<MealType> order = Comparator
                .comparingInt(MealType::priority).reversed()
                .thenComparing(Comparator.comparing(MealType::isShaped).reversed())
                .thenComparing(type -> type.id().toString());
        this.craftingTableTypes = types.values().stream()
                .filter(type -> type.station() == MealType.Station.CRAFTING_TABLE)
                .sorted(order)
                .toList();
        this.cookingPotTypes = types.values().stream()
                .filter(type -> type.station() == MealType.Station.COOKING_POT)
                .sorted(order)
                .toList();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        types.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            if (!(entry.getValue() instanceof JsonObject obj)) {
                Constants.LOG.warn("Invalid meal_types file (must be JSON object): {}", fileId);
                continue;
            }

            Optional<MealType> parsed = MealType.CODEC.parse(JsonOps.INSTANCE, obj)
                    .resultOrPartial(err -> Constants.LOG.error("Failed to parse meal type {}: {}", fileId, err));
            if (parsed.isEmpty()) {
                continue;
            }

            MealType type = parsed.get();
            if (type.resolveItem().isEmpty()) {
                Constants.LOG.warn("Meal type {} names an unknown result item {}, skipping it",
                        type.id(), type.item());
                continue;
            }
            types.put(type.id(), type);
        }

        reindex();
        Constants.LOG.info("Loaded {} meal types ({} on the crafting table, {} in the cooking pot)",
                types.size(), craftingTableTypes.size(), cookingPotTypes.size());
    }
}
