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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Loads the {@link MealFormula} from {@code data/<namespace>/modest_meals/formula/}.
 */
public class MealFormulaManager extends SimpleJsonResourceReloadListener {
    public static final MealFormulaManager INSTANCE = new MealFormulaManager();

    private volatile MealFormula formula = MealFormula.DEFAULT;

    public MealFormulaManager() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "modest_meals/formula");
    }

    public static MealFormula get() {
        return INSTANCE.formula;
    }

    public static void applyFromNetwork(MealFormula formula) {
        INSTANCE.formula = formula;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        MealFormula resolved = MealFormula.DEFAULT;

        List<Map.Entry<ResourceLocation, JsonElement>> files = resources.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .toList();

        for (Map.Entry<ResourceLocation, JsonElement> file : files) {
            if (!(file.getValue() instanceof JsonObject obj)) {
                Constants.LOG.warn("Invalid formula file (must be JSON object): {}", file.getKey());
                continue;
            }
            var parsed = MealFormula.CODEC.parse(JsonOps.INSTANCE, obj)
                    .resultOrPartial(err -> Constants.LOG.error("Failed to parse meal formula {}: {}",
                            file.getKey(), err));
            if (parsed.isPresent()) {
                resolved = parsed.get();
            }
        }

        this.formula = resolved;
        Constants.LOG.info("Loaded meal formula ({}s per ingredient, {}s cap)",
                resolved.secondsPerIngredient(), resolved.maxDurationSeconds());
    }
}
