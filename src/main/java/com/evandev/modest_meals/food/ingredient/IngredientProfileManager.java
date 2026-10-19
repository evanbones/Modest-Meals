package com.evandev.modest_meals.food.ingredient;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.FoodProfileManager;
import com.evandev.modest_meals.food.FoodValues;
import com.evandev.modest_meals.tag.ModItemTags;
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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads {@link IngredientProfile}s from {@code data/<namespace>/modest_meals/ingredients/} and resolves the
 * best one for a given item.
 */
public class IngredientProfileManager extends SimpleJsonResourceReloadListener {
    public static final IngredientProfileManager INSTANCE = new IngredientProfileManager();

    private static final ResourceLocation CUSTOM_FILE_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "custom_ingredients");

    private final Map<ResourceLocation, IngredientProfile> itemProfiles = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, IngredientProfile> baselineItemProfiles = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, IngredientProfile> customItemProfiles = new ConcurrentHashMap<>();
    private volatile List<TagEntry> tagProfiles = List.of();
    private volatile List<TagEntry> baselineTagProfiles = List.of();
    private volatile List<TagEntry> customTagProfiles = List.of();

    public IngredientProfileManager() {
        super(new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(), "modest_meals/ingredients");
    }

    public static Optional<IngredientProfile> authored(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return authored(stack.getItem());
    }

    public static Optional<IngredientProfile> authored(Item item) {
        IngredientProfile direct = INSTANCE.itemProfiles.get(BuiltInRegistries.ITEM.getKey(item));
        if (direct != null) {
            return Optional.of(direct);
        }
        Holder<Item> holder = item.builtInRegistryHolder();
        for (TagEntry entry : INSTANCE.tagProfiles) {
            if (holder.is(entry.tag())) {
                return Optional.of(entry.profile());
            }
        }
        return Optional.empty();
    }

    public static Optional<IngredientProfile> resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }

        Optional<IngredientProfile> authored = authored(stack.getItem());
        Optional<Derived> derived = derive(stack);

        if (authored.isEmpty()) {
            return derived.map(d -> IngredientProfile.EMPTY.withDerived(d.health(), d.stamina(), d.digestTicks()));
        }

        Derived d = derived.orElse(Derived.NONE);
        return authored.map(profile -> profile.withDerived(d.health(), d.stamina(), d.digestTicks()));
    }

    private static Optional<Derived> derive(ItemStack stack) {
        int nutrition = FoodValues.nutritionOf(stack);
        if (nutrition <= 0) {
            return Optional.empty();
        }
        return FoodProfileManager.resolve(stack).map(profile -> {
            float health = profile.healthFor(nutrition);
            return new Derived(health, profile.staminaFor(nutrition), profile.digestTicksFor(health));
        });
    }

    public static Optional<Integer> eatTicks(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return authored(stack.getItem())
                .flatMap(IngredientProfile::eatSeconds)
                .filter(seconds -> seconds > 0.0F)
                .map(seconds -> Math.max(1, Math.round(seconds * 20.0F)));
    }

    public static boolean isIngredient(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.is(ModItemTags.NOT_AN_INGREDIENT) && !hasCustomProfile(stack.getItem())) {
            return false;
        }
        return resolve(stack).isPresent();
    }

    private static boolean hasCustomProfile(Item item) {
        if (INSTANCE.customItemProfiles.containsKey(BuiltInRegistries.ITEM.getKey(item))) {
            return true;
        }
        Holder<Item> holder = item.builtInRegistryHolder();
        for (TagEntry entry : INSTANCE.customTagProfiles) {
            if (holder.is(entry.tag())) {
                return true;
            }
        }
        return false;
    }

    public static Map<ResourceLocation, IngredientProfile> snapshotItemProfiles() {
        return Map.copyOf(INSTANCE.itemProfiles);
    }

    public static List<TagEntry> snapshotTagProfiles() {
        return INSTANCE.tagProfiles;
    }

    public static void setItemProfile(ResourceLocation itemId, @Nullable IngredientProfile profile) {
        if (profile == null) {
            INSTANCE.itemProfiles.remove(itemId);
        } else {
            INSTANCE.itemProfiles.put(itemId, profile);
        }
    }

    public static void applyFromNetwork(Map<ResourceLocation, IngredientProfile> itemProfiles,
                                        List<TagEntry> tagProfiles, boolean updateBaseline) {
        INSTANCE.itemProfiles.clear();
        INSTANCE.itemProfiles.putAll(itemProfiles);
        INSTANCE.tagProfiles = sorted(tagProfiles);
        if (updateBaseline) {
            INSTANCE.baselineItemProfiles.clear();
            INSTANCE.baselineItemProfiles.putAll(itemProfiles);
            INSTANCE.baselineTagProfiles = INSTANCE.tagProfiles;
        }
    }

    public static void restoreBaseline() {
        INSTANCE.itemProfiles.clear();
        INSTANCE.itemProfiles.putAll(INSTANCE.baselineItemProfiles);
        INSTANCE.tagProfiles = INSTANCE.baselineTagProfiles;
    }

    private static List<TagEntry> sorted(List<TagEntry> entries) {
        List<TagEntry> copy = new ArrayList<>(entries);
        copy.sort(TagEntry.ORDER);
        return List.copyOf(copy);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        itemProfiles.clear();
        baselineItemProfiles.clear();
        customItemProfiles.clear();

        List<TagEntry> tags = new ArrayList<>();
        List<TagEntry> baselineTags = new ArrayList<>();
        List<TagEntry> customTags = new ArrayList<>();

        List<Map.Entry<ResourceLocation, JsonElement>> files = new ArrayList<>(resources.entrySet());
        files.sort(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)));

        for (Map.Entry<ResourceLocation, JsonElement> file : files) {
            ResourceLocation fileId = file.getKey();
            if (!(file.getValue() instanceof JsonObject obj)
                    || !obj.has("entries") || !obj.get("entries").isJsonArray()) {
                Constants.LOG.warn("Ingredients file {} has no 'entries' array", fileId);
                continue;
            }

            boolean isBaseline = !fileId.equals(CUSTOM_FILE_ID);
            JsonArray entries = obj.getAsJsonArray("entries");
            for (JsonElement element : entries) {
                if (!(element instanceof JsonObject entry) || !entry.has("target")) {
                    continue;
                }
                String target = entry.get("target").getAsString();
                int priority = entry.has("priority") ? entry.get("priority").getAsInt() : 0;

                Optional<IngredientProfile> parsed = IngredientProfile.CODEC.parse(JsonOps.INSTANCE, entry)
                        .resultOrPartial(err -> Constants.LOG.error("Failed to parse ingredient {} in {}: {}",
                                target, fileId, err));
                if (parsed.isEmpty()) {
                    continue;
                }
                IngredientProfile profile = parsed.get();

                if (target.startsWith("#")) {
                    TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(target.substring(1)));
                    TagEntry tagEntry = new TagEntry(tag, priority, profile);
                    tags.add(tagEntry);
                    if (isBaseline) {
                        baselineTags.add(tagEntry);
                    } else {
                        customTags.add(tagEntry);
                    }
                } else {
                    ResourceLocation itemId = ResourceLocation.parse(target);
                    itemProfiles.put(itemId, profile);
                    if (isBaseline) {
                        baselineItemProfiles.put(itemId, profile);
                    } else {
                        customItemProfiles.put(itemId, profile);
                    }
                }
            }
        }

        this.tagProfiles = sorted(tags);
        this.baselineTagProfiles = sorted(baselineTags);
        this.customTagProfiles = sorted(customTags);

        Constants.LOG.info("Loaded ingredient profiles for {} items and {} item tags",
                itemProfiles.size(), tagProfiles.size());
    }

    public record TagEntry(TagKey<Item> tag, int priority, IngredientProfile profile) {
        public static final Codec<TagEntry> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagEntry::tag),
                        Codec.INT.optionalFieldOf("priority", 0).forGetter(TagEntry::priority),
                        IngredientProfile.CODEC.fieldOf("profile").forGetter(TagEntry::profile)
                ).apply(instance, TagEntry::new));

        public static final Comparator<TagEntry> ORDER = Comparator
                .comparingInt(TagEntry::priority).reversed()
                .thenComparing(entry -> entry.tag().location().toString());
    }

    private record Derived(float health, float stamina, int digestTicks) {
        static final Derived NONE = new Derived(0.0F, 0.0F, 0);
    }
}
