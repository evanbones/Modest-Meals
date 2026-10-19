package com.evandev.modest_meals.food.ingredient;

import com.evandev.modest_meals.Constants;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads {@link MealEffect}s from {@code data/<namespace>/modest_meals/meal_effects/}.
 */
public class MealEffectManager extends SimpleJsonResourceReloadListener {
    public static final MealEffectManager INSTANCE = new MealEffectManager();

    private final Map<ResourceLocation, MealEffect> effects = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, MealEffect> baseline = new ConcurrentHashMap<>();

    public MealEffectManager() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "modest_meals/meal_effects");
    }

    public static Optional<MealEffect> get(ResourceLocation id) {
        return Optional.ofNullable(INSTANCE.effects.get(id));
    }

    public static Collection<MealEffect> all() {
        return List.copyOf(INSTANCE.effects.values());
    }

    public static Map<ResourceLocation, MealEffect> snapshot() {
        return Map.copyOf(INSTANCE.effects);
    }

    public static void applyFromNetwork(Map<ResourceLocation, MealEffect> loaded, boolean updateBaseline) {
        INSTANCE.effects.clear();
        INSTANCE.effects.putAll(loaded);
        if (updateBaseline) {
            INSTANCE.baseline.clear();
            INSTANCE.baseline.putAll(loaded);
        }
    }

    public static void restoreBaseline() {
        INSTANCE.effects.clear();
        INSTANCE.effects.putAll(INSTANCE.baseline);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        effects.clear();
        baseline.clear();

        int stubs = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            if (!(entry.getValue() instanceof JsonObject obj)) {
                Constants.LOG.warn("Invalid meal_effects file (must be JSON object): {}", fileId);
                continue;
            }

            Optional<MealEffect> parsed = MealEffect.CODEC.parse(JsonOps.INSTANCE, obj)
                    .resultOrPartial(err -> Constants.LOG.error("Failed to parse meal effect {}: {}", fileId, err));
            if (parsed.isEmpty()) {
                continue;
            }

            MealEffect effect = parsed.get();
            if (!effect.isUsable()) {
                stubs++;
                Constants.LOG.debug("Meal effect {} has no resolvable mob effect ({}); loading it as a stub",
                        effect.id(), effect.mobEffect().map(ResourceLocation::toString).orElse("none declared"));
            }
            effects.put(effect.id(), effect);
            baseline.put(effect.id(), effect);
        }

        Constants.LOG.info("Loaded {} meal effects ({} stubbed)", effects.size(), stubs);
    }
}
