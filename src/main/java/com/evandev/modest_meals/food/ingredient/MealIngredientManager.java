package com.evandev.modest_meals.food.ingredient;

import com.evandev.modest_meals.Constants;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads {@link MealIngredientVisual} definitions from {@code data/<namespace>/modest_meals/meal_ingredients/}
 * and resolves visuals for meal ingredients by item or tag.
 */
public class MealIngredientManager extends SimpleJsonResourceReloadListener {
    public static final MealIngredientManager INSTANCE = new MealIngredientManager();

    private final Map<ResourceLocation, MealIngredientVisual> itemVisuals = new ConcurrentHashMap<>();
    private volatile List<TagEntry> tagVisuals = List.of();

    public MealIngredientManager() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "modest_meals/meal_ingredients");
    }

    public static Optional<MealIngredientVisual> resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return resolve(stack.getItem());
    }

    public static Optional<MealIngredientVisual> resolve(Item item) {
        MealIngredientVisual direct = INSTANCE.itemVisuals.get(BuiltInRegistries.ITEM.getKey(item));
        if (direct != null) {
            return Optional.of(direct);
        }
        Holder<Item> holder = item.builtInRegistryHolder();
        for (TagEntry entry : INSTANCE.tagVisuals) {
            if (holder.is(entry.tag())) {
                return Optional.of(entry.visual());
            }
        }
        return Optional.empty();
    }

    public static boolean isSupported(ResourceLocation mealTypeId, ItemStack stack) {
        return resolve(stack).map(v -> v.supportsMeal(mealTypeId)).orElse(false);
    }

    public static boolean isSupported(ResourceLocation mealTypeId, Item item) {
        return resolve(item).map(v -> v.supportsMeal(mealTypeId)).orElse(false);
    }

    public static Optional<ResourceLocation> getIngredientSprite(ResourceLocation mealTypeId, ItemStack stack, int slotIndex, int totalSlots) {
        return resolve(stack).flatMap(v -> v.getSprite(mealTypeId, slotIndex, totalSlots));
    }

    public static Optional<ResourceLocation> getIngredientSprite(ResourceLocation mealTypeId, Item item, int slotIndex, int totalSlots) {
        return resolve(item).flatMap(v -> v.getSprite(mealTypeId, slotIndex, totalSlots));
    }

    public static Map<ResourceLocation, MealIngredientVisual> snapshotItemVisuals() {
        return Map.copyOf(INSTANCE.itemVisuals);
    }

    public static List<TagEntry> snapshotTagVisuals() {
        return INSTANCE.tagVisuals;
    }

    public static void applyFromNetwork(Map<ResourceLocation, MealIngredientVisual> itemVisuals,
                                        List<TagEntry> tagVisuals) {
        INSTANCE.itemVisuals.clear();
        INSTANCE.itemVisuals.putAll(itemVisuals);
        INSTANCE.tagVisuals = sorted(tagVisuals);
    }

    private static List<TagEntry> sorted(List<TagEntry> entries) {
        List<TagEntry> copy = new ArrayList<>(entries);
        copy.sort(TagEntry.ORDER);
        return List.copyOf(copy);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        itemVisuals.clear();
        List<TagEntry> tags = new ArrayList<>();

        List<Map.Entry<ResourceLocation, JsonElement>> files = new ArrayList<>(resources.entrySet());
        files.sort(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)));

        for (Map.Entry<ResourceLocation, JsonElement> file : files) {
            ResourceLocation fileId = file.getKey();
            if (!(file.getValue() instanceof JsonObject obj)) {
                continue;
            }

            if (obj.has("entries") && obj.get("entries").isJsonArray()) {
                JsonArray entries = obj.getAsJsonArray("entries");
                for (JsonElement element : entries) {
                    if (element instanceof JsonObject entry) {
                        parseEntry(fileId, entry, tags);
                    }
                }
            } else {
                parseEntry(fileId, obj, tags);
            }
        }

        tagVisuals = sorted(tags);
    }

    private void parseEntry(ResourceLocation fileId, JsonObject entry, List<TagEntry> tags) {
        String target = null;
        if (entry.has("target")) {
            target = entry.get("target").getAsString();
        } else if (entry.has("tag")) {
            target = "#" + entry.get("tag").getAsString();
        } else if (entry.has("item")) {
            target = entry.get("item").getAsString();
        }

        if (target == null || target.isBlank()) {
            Constants.LOG.warn("Meal ingredient entry in {} is missing target/item/tag", fileId);
            return;
        }

        int priority = entry.has("priority") ? entry.get("priority").getAsInt() : 0;
        Optional<MealIngredientVisual> parsed = MealIngredientVisual.CODEC.parse(JsonOps.INSTANCE, entry)
                .resultOrPartial(err -> Constants.LOG.error("Failed to parse meal ingredient visual in {}: {}", fileId, err));

        if (parsed.isEmpty()) {
            return;
        }

        MealIngredientVisual visual = parsed.get();
        if (target.startsWith("#")) {
            ResourceLocation tagLoc = ResourceLocation.parse(target.substring(1));
            TagKey<Item> tag = TagKey.create(Registries.ITEM, tagLoc);
            tags.add(new TagEntry(tag, priority, visual));
        } else {
            ResourceLocation itemLoc = ResourceLocation.parse(target);
            itemVisuals.put(itemLoc, visual);
        }
    }

    public record TagEntry(TagKey<Item> tag, int priority, MealIngredientVisual visual) {
        public static final Comparator<TagEntry> ORDER = Comparator
                .comparingInt(TagEntry::priority).reversed()
                .thenComparing(entry -> entry.tag().location().toString());

        public static final Codec<TagEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagEntry::tag),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(TagEntry::priority),
                MealIngredientVisual.CODEC.fieldOf("visual").forGetter(TagEntry::visual)
        ).apply(instance, TagEntry::new));
    }
}
