package com.evandev.modest_meals.food;

import com.evandev.modest_meals.Constants;
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

import java.util.*;

/**
 * Loads {@link FoodProfile}s from {@code data/<namespace>/food_profiles/} and resolves the best one for
 * a given food.
 */
public class FoodProfileManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final FoodProfileManager INSTANCE = new FoodProfileManager();
    private volatile List<FoodProfile> profiles = List.of();

    public FoodProfileManager() {
        super(GSON, "food_profiles");
    }

    public static Optional<FoodProfile> resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        Holder<Item> holder = stack.getItem().builtInRegistryHolder();
        for (FoodProfile profile : INSTANCE.profiles) {
            if (matches(profile, stack, holder)) {
                return Optional.of(profile);
            }
        }
        return Optional.empty();
    }

    private static boolean matches(FoodProfile profile, ItemStack stack, Holder<Item> holder) {
        String match = profile.match();
        if (FoodProfile.MATCH_ANY.equals(match)) {
            return true;
        }
        if (match.startsWith("#")) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(match.substring(1)));
            return holder.is(tag);
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return itemId.toString().equals(match);
    }

    public static List<FoodProfile> snapshotProfiles() {
        return INSTANCE.profiles;
    }

    public static void applyFromNetwork(List<FoodProfile> profiles) {
        INSTANCE.profiles = List.copyOf(profiles);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<FoodProfile> loaded = new ArrayList<>();

        for (var entry : resources.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            JsonElement json = entry.getValue();

            if (!json.isJsonObject()) {
                Constants.LOG.warn("Invalid food_profiles file (must be JSON object): {}", fileId);
                continue;
            }

            JsonObject obj = json.getAsJsonObject();
            if (!obj.has("profiles") || !obj.get("profiles").isJsonArray()) {
                Constants.LOG.warn("food_profiles file {} has no 'profiles' array", fileId);
                continue;
            }

            JsonArray array = obj.getAsJsonArray("profiles");
            for (JsonElement element : array) {
                FoodProfile.CODEC.parse(JsonOps.INSTANCE, element)
                        .resultOrPartial(err -> Constants.LOG.error("Failed to parse food profile in {}: {}", fileId, err))
                        .ifPresent(loaded::add);
            }
        }

        loaded.sort(Comparator.comparingInt(FoodProfile::priority).reversed()
                .thenComparing(FoodProfile::id));
        this.profiles = List.copyOf(loaded);

        Constants.LOG.info("Loaded {} food profiles", this.profiles.size());
    }
}
