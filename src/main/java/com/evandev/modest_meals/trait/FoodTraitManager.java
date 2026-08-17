package com.evandev.modest_meals.trait;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.component.ModDataComponents;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FoodTraitManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final FoodTraitManager INSTANCE = new FoodTraitManager();
    private final Map<ResourceLocation, List<FoodTrait>> itemTraits = new ConcurrentHashMap<>();
    private final Map<TagKey<Item>, List<FoodTrait>> tagTraits = new ConcurrentHashMap<>();

    public FoodTraitManager() {
        super(GSON, "food_traits");
    }

    public static List<FoodTrait> getTraits(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Collections.emptyList();
        }

        if (stack.has(ModDataComponents.FOOD_TRAITS.get())) {
            FoodTraitsData data = stack.get(ModDataComponents.FOOD_TRAITS.get());
            if (data != null) {
                return data.traits();
            }
        }

        return getTraits(stack.getItem(), stack);
    }

    public static List<FoodTrait> getTraits(Item item) {
        return getTraits(item, new ItemStack(item));
    }

    public static List<FoodTrait> getTraits(Item item, ItemStack stack) {
        List<FoodTrait> result = new ArrayList<>();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

        List<FoodTrait> direct = INSTANCE.itemTraits.get(itemId);
        if (direct != null) {
            result.addAll(direct);
        }

        Holder<Item> itemHolder = item.builtInRegistryHolder();
        for (Map.Entry<TagKey<Item>, List<FoodTrait>> entry : INSTANCE.tagTraits.entrySet()) {
            if (itemHolder.is(entry.getKey())) {
                result.addAll(entry.getValue());
            }
        }

        return result;
    }

    public static boolean hasTraits(ItemStack stack) {
        return !getTraits(stack).isEmpty();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        itemTraits.clear();
        tagTraits.clear();

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
                        if (target != null && traitsArr != null) {
                            parseAndAdd(target, traitsArr);
                        }
                    }
                }
            } else {
                for (Map.Entry<String, JsonElement> prop : obj.entrySet()) {
                    String target = prop.getKey();
                    if (prop.getValue().isJsonArray()) {
                        parseAndAdd(target, prop.getValue().getAsJsonArray());
                    }
                }
            }
        }

        Constants.LOG.info("Loaded food traits for {} items and {} item tags", itemTraits.size(), tagTraits.size());
    }

    private void parseAndAdd(String target, JsonArray traitsArray) {
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
        } else {
            ResourceLocation itemLoc = ResourceLocation.parse(target);
            itemTraits.computeIfAbsent(itemLoc, k -> new ArrayList<>()).addAll(parsedList);
        }
    }
}
