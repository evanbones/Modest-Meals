package com.evandev.modest_meals.food.meal;

import com.evandev.modest_meals.Constants;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
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

    private static final RegistryOps<JsonElement> BUILT_IN_OPS =
            RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));

    private final Map<ResourceLocation, MealType> types = new ConcurrentHashMap<>();
    private volatile List<MealType> craftingTableTypes = List.of();
    private volatile List<MealType> cookingPotTypes = List.of();
    private volatile List<MealType> vatTypes = List.of();

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

    public static List<MealType> vat() {
        return INSTANCE.vatTypes;
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
        this.craftingTableTypes = atStation(MealType.Station.CRAFTING_TABLE, order);
        this.cookingPotTypes = atStation(MealType.Station.COOKING_POT, order);
        this.vatTypes = atStation(MealType.Station.VAT, order);
    }

    private List<MealType> atStation(MealType.Station station, Comparator<MealType> order) {
        return types.values().stream()
                .filter(type -> type.station() == station)
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

            Optional<MealType> parsed = MealType.CODEC.parse(BUILT_IN_OPS, obj)
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
            if (!type.station().isAvailable()) {
                Constants.LOG.debug("Skipping meal type {}: its station {} is not in this game",
                        type.id(), type.station().getSerializedName());
                continue;
            }
            types.put(type.id(), type);
        }

        reindex();
        Constants.LOG.info("Loaded {} meal types ({} on the crafting table, {} in the cooking pot, {} in the vat)",
                types.size(), craftingTableTypes.size(), cookingPotTypes.size(), vatTypes.size());
    }
}
