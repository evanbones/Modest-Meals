package com.evandev.modest_meals.trait;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.food.FoodValues;
import com.evandev.modest_meals.trait.impl.EffectGrantTrait;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FoodTraitManager extends SimpleJsonResourceReloadListener {
    public static final FoodTraitManager INSTANCE = new FoodTraitManager();

    private static final ResourceLocation CUSTOM_FILE_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_traits");

    private final Map<ResourceLocation, List<FoodTrait>> itemTraits = new ConcurrentHashMap<>();
    private final Map<TagKey<Item>, List<FoodTrait>> tagTraits = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, Set<String>> itemSuppressions = new ConcurrentHashMap<>();

    private final Map<ResourceLocation, List<FoodTrait>> baselineItemTraits = new ConcurrentHashMap<>();
    private final Map<TagKey<Item>, List<FoodTrait>> baselineTagTraits = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, Set<String>> baselineSuppressions = new ConcurrentHashMap<>();

    public FoodTraitManager() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "food_traits");
    }

    public static List<FoodTrait> getTraits(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Collections.emptyList();
        }

        return getTraits(stack.getItem());
    }

    public static List<FoodTrait> getTraits(Item item) {
        return merge(item, INSTANCE.itemTraits, INSTANCE.tagTraits, suppressionsFor(item));
    }

    public static List<FoodTrait> getBaselineItemTraits(Item item) {
        List<FoodTrait> direct = INSTANCE.baselineItemTraits.get(BuiltInRegistries.ITEM.getKey(item));
        return direct == null ? List.of() : List.copyOf(direct);
    }

    public static List<FoodTrait> getBaselineTagTraits(Item item) {
        return merge(item, Map.of(), INSTANCE.baselineTagTraits, Set.of());
    }

    private static List<FoodTrait> merge(Item item, Map<ResourceLocation, List<FoodTrait>> itemSource,
                                         Map<TagKey<Item>, List<FoodTrait>> tagSource, Set<String> suppressed) {
        Map<String, FoodTrait> resolved = new LinkedHashMap<>();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        List<FoodTrait> direct = itemSource.get(itemId);
        if (direct != null) {
            for (FoodTrait trait : direct) {
                resolved.putIfAbsent(getMergeKey(trait), trait);
            }
        }

        Holder<Item> itemHolder = item.builtInRegistryHolder();
        for (Map.Entry<TagKey<Item>, List<FoodTrait>> entry : tagSource.entrySet()) {
            if (itemHolder.is(entry.getKey())) {
                for (FoodTrait trait : entry.getValue()) {
                    resolved.putIfAbsent(getMergeKey(trait), trait);
                }
            }
        }

        resolved.keySet().removeAll(suppressed);
        return new ArrayList<>(resolved.values());
    }

    public static String getMergeKey(FoodTrait trait) {
        if (trait instanceof EffectGrantTrait effectGrant) {
            return "effect_grant:" + effectGrant.effect().getRegisteredName();
        }
        return mergeKeyOf(trait.getType());
    }

    public static String mergeKeyOf(FoodTraitType<?> type) {
        ResourceLocation key = FoodTraitType.REGISTRY.getKey(type);
        return key == null ? String.valueOf(type) : key.toString();
    }

    public static Set<String> suppressionsFor(Item item) {
        return suppressionsFor(BuiltInRegistries.ITEM.getKey(item));
    }

    public static Set<String> suppressionsFor(ResourceLocation itemId) {
        return INSTANCE.itemSuppressions.getOrDefault(itemId, Set.of());
    }

    public static Set<String> getBaselineSuppressions(ResourceLocation itemId) {
        return INSTANCE.baselineSuppressions.getOrDefault(itemId, Set.of());
    }

    public static void setSuppressions(ResourceLocation itemId, Collection<String> mergeKeys) {
        if (mergeKeys == null || mergeKeys.isEmpty()) {
            INSTANCE.itemSuppressions.remove(itemId);
        } else {
            INSTANCE.itemSuppressions.put(itemId, Set.copyOf(mergeKeys));
        }
    }

    public static Map<ResourceLocation, List<String>> snapshotSuppressions() {
        Map<ResourceLocation, List<String>> copy = new LinkedHashMap<>();
        INSTANCE.itemSuppressions.forEach((id, keys) -> copy.put(id, List.copyOf(keys)));
        return copy;
    }

    public static Map<ResourceLocation, List<FoodTrait>> snapshotItemTraits() {
        return Map.copyOf(INSTANCE.itemTraits);
    }

    public static Map<TagKey<Item>, List<FoodTrait>> snapshotTagTraits() {
        return Map.copyOf(INSTANCE.tagTraits);
    }

    public static void applyFromNetwork(Map<ResourceLocation, List<FoodTrait>> itemTraits, Map<TagKey<Item>, List<FoodTrait>> tagTraits,
                                        Map<ResourceLocation, List<String>> suppressions, boolean updateBaseline) {
        INSTANCE.itemTraits.clear();
        INSTANCE.itemTraits.putAll(itemTraits);
        INSTANCE.tagTraits.clear();
        INSTANCE.tagTraits.putAll(tagTraits);
        INSTANCE.itemSuppressions.clear();
        suppressions.forEach((id, keys) -> INSTANCE.itemSuppressions.put(id, Set.copyOf(keys)));
        if (updateBaseline) {
            INSTANCE.baselineItemTraits.clear();
            INSTANCE.baselineItemTraits.putAll(itemTraits);
            INSTANCE.baselineTagTraits.clear();
            INSTANCE.baselineTagTraits.putAll(tagTraits);
            INSTANCE.baselineSuppressions.clear();
            INSTANCE.baselineSuppressions.putAll(INSTANCE.itemSuppressions);
        }
    }

    public static void restoreBaseline() {
        INSTANCE.itemTraits.clear();
        INSTANCE.itemTraits.putAll(INSTANCE.baselineItemTraits);
        INSTANCE.tagTraits.clear();
        INSTANCE.tagTraits.putAll(INSTANCE.baselineTagTraits);
        INSTANCE.itemSuppressions.clear();
        INSTANCE.itemSuppressions.putAll(INSTANCE.baselineSuppressions);
    }

    public static void setItemTraits(ResourceLocation itemId, List<FoodTrait> traits) {
        if (traits == null || traits.isEmpty()) {
            INSTANCE.itemTraits.remove(itemId);
        } else {
            INSTANCE.itemTraits.put(itemId, new ArrayList<>(traits));
        }
    }

    public static void removeItemTraits(ResourceLocation itemId) {
        INSTANCE.itemTraits.remove(itemId);
    }

    public static boolean hasTraits(ItemStack stack) {
        return !getTraits(stack).isEmpty();
    }

    public static void applyAll(LivingEntity entity, ItemStack stack) {
        applyAll(entity, stack, 1.0F);
    }

    public static void applyAll(LivingEntity entity, ItemStack stack, float valueScale) {
        List<FoodTrait> traits = FoodValues.effectiveTraits(stack);
        if (traits.isEmpty()) {
            return;
        }
        float valMult = ModConfig.get().traitGlobalValueMultiplier * valueScale;
        float durMult = ModConfig.get().traitGlobalDurationMultiplier;
        for (FoodTrait trait : traits) {
            trait.apply(entity, stack, valMult, durMult);
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        itemTraits.clear();
        tagTraits.clear();
        itemSuppressions.clear();
        baselineItemTraits.clear();
        baselineTagTraits.clear();
        baselineSuppressions.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            JsonElement json = entry.getValue();

            if (!json.isJsonObject()) {
                Constants.LOG.warn("Invalid food_traits file (must be JSON object): {}", fileId);
                continue;
            }

            JsonObject obj = json.getAsJsonObject();
            if (obj.has("entries") && obj.get("entries").isJsonArray()) {
                JsonArray entries = obj.getAsJsonArray("entries");
                for (JsonElement element : entries) {
                    if (element.isJsonObject()) {
                        JsonObject entryObj = element.getAsJsonObject();
                        String target = entryObj.has("target") ? entryObj.get("target").getAsString() : null;
                        JsonArray traitsArr = entryObj.has("traits") && entryObj.get("traits").isJsonArray()
                                ? entryObj.getAsJsonArray("traits") : null;
                        boolean baseline = !fileId.equals(CUSTOM_FILE_ID);
                        if (target != null && traitsArr != null) {
                            parseAndAdd(target, traitsArr, baseline);
                        }
                        if (target != null && entryObj.has("suppress") && entryObj.get("suppress").isJsonArray()) {
                            parseSuppressions(target, entryObj.getAsJsonArray("suppress"), baseline);
                        }
                    }
                }
            } else {
                for (Map.Entry<String, JsonElement> prop : obj.entrySet()) {
                    String target = prop.getKey();
                    if (prop.getValue().isJsonArray()) {
                        parseAndAdd(target, prop.getValue().getAsJsonArray(), !fileId.equals(CUSTOM_FILE_ID));
                    }
                }
            }
        }

        Constants.LOG.info("Loaded food traits for {} items and {} item tags", itemTraits.size(), tagTraits.size());
    }

    private void parseSuppressions(String target, JsonArray keys, boolean baseline) {
        if (target.startsWith("#")) {
            Constants.LOG.warn("'suppress' is only supported on item targets, ignoring it for {}", target);
            return;
        }

        Set<String> parsed = new LinkedHashSet<>();
        for (JsonElement key : keys) {
            if (key.isJsonPrimitive()) parsed.add(key.getAsString());
        }
        if (parsed.isEmpty()) return;

        ResourceLocation itemLoc = ResourceLocation.parse(target);
        itemSuppressions.computeIfAbsent(itemLoc, k -> ConcurrentHashMap.newKeySet()).addAll(parsed);
        if (baseline) {
            baselineSuppressions.computeIfAbsent(itemLoc, k -> ConcurrentHashMap.newKeySet()).addAll(parsed);
        }
    }

    private void parseAndAdd(String target, JsonArray traitsArray, boolean baseline) {
        List<FoodTrait> parsedList = new ArrayList<>();
        for (JsonElement traitEl : traitsArray) {
            if (traitEl.isJsonObject()) {
                FoodTraitType.CODEC.parse(JsonOps.INSTANCE, traitEl)
                        .resultOrPartial(err -> Constants.LOG.error("Failed to parse food trait in {}: {}", target, err))
                        .ifPresent(parsedList::add);
            }
        }

        if (parsedList.isEmpty()) return;

        if (target.startsWith("#")) {
            ResourceLocation tagLoc = ResourceLocation.parse(target.substring(1));
            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagLoc);
            tagTraits.computeIfAbsent(tagKey, k -> new ArrayList<>()).addAll(parsedList);
            if (baseline) {
                baselineTagTraits.computeIfAbsent(tagKey, k -> new ArrayList<>()).addAll(parsedList);
            }
        } else {
            ResourceLocation itemLoc = ResourceLocation.parse(target);
            itemTraits.computeIfAbsent(itemLoc, k -> new ArrayList<>()).addAll(parsedList);
            if (baseline) {
                baselineItemTraits.computeIfAbsent(itemLoc, k -> new ArrayList<>()).addAll(parsedList);
            }
        }
    }
}
