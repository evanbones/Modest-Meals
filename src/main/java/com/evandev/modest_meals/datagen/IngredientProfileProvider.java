package com.evandev.modest_meals.datagen;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.ingredient.IngredientProfile;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class IngredientProfileProvider implements DataProvider {

    private static final int WEAK = 7;
    private static final int MEDIUM = 14;
    private static final int STRONG = 21;

    private final PackOutput.PathProvider pathProvider;

    public IngredientProfileProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "modest_meals/ingredients");
    }

    private static IngredientProfile food(Float health, Float stamina) {
        return build(health, stamina, 0, Optional.empty(), 0, 0.0F, 0.0F, 0);
    }

    private static IngredientProfile effect(Float health, Float stamina, String axis, int potency) {
        return build(health, stamina, 0,
                Optional.of(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, axis)), potency,
                0.0F, 0.0F, 0);
    }

    private static IngredientProfile temporary(Float health, Float stamina, int digestTicks,
                                               float temporaryHealth, float temporaryStamina) {
        return build(health, stamina, digestTicks, Optional.empty(), 0,
                temporaryHealth, temporaryStamina, 0);
    }

    private static IngredientProfile timeBoost(Float health, Float stamina, int seconds) {
        return build(health, stamina, 0, Optional.empty(), 0, 0.0F, 0.0F, seconds);
    }

    private static IngredientProfile dish(Float health, Float stamina) {
        return dish(health, stamina, 0);
    }

    private static IngredientProfile dish(Float health, Float stamina, int digestTicks) {
        return build(health, stamina, digestTicks, Optional.empty(), 0, 0.0F, 0.0F, 0);
    }

    private static IngredientProfile build(Float health, Float stamina, int digestTicks,
                                           Optional<ResourceLocation> effect, int potency,
                                           float temporaryHealth, float temporaryStamina, int timeBonus) {
        return new IngredientProfile(
                Optional.ofNullable(health), Optional.ofNullable(stamina),
                digestTicks > 0 ? Optional.of(digestTicks) : Optional.empty(),
                Optional.empty(),
                effect, potency, temporaryHealth, temporaryStamina, timeBonus);
    }

    private static String id(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<String, Builder> files = new LinkedHashMap<>();
        crops(files.computeIfAbsent("crops", key -> new Builder()));
        proteins(files.computeIfAbsent("proteins", key -> new Builder()));
        fruitsAndSweets(files.computeIfAbsent("fruits_and_sweets", key -> new Builder()));
        staples(files.computeIfAbsent("staples", key -> new Builder()));
        misc(files.computeIfAbsent("misc", key -> new Builder()));
        dishes(files.computeIfAbsent("dishes", key -> new Builder()));

        List<CompletableFuture<?>> futures = new ArrayList<>();
        files.forEach((name, builder) -> {
            JsonArray array = new JsonArray();
            for (Entry entry : builder.entries) {
                JsonObject json = IngredientProfile.CODEC
                        .encodeStart(JsonOps.INSTANCE, entry.profile())
                        .getOrThrow(error -> new IllegalStateException(
                                "Failed to encode ingredient " + entry.target() + ": " + error))
                        .getAsJsonObject();
                json.addProperty("target", entry.target());
                if (entry.priority() != 0) {
                    json.addProperty("priority", entry.priority());
                }
                array.add(json);
            }
            JsonObject root = new JsonObject();
            root.add("entries", array);

            Path path = pathProvider.json(
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name));
            futures.add(DataProvider.saveStable(output, root, path));
        });

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void crops(Builder builder) {
        builder.addTag("c:crops/wheat", 145, food(1.0F, 4.0F));
        builder.add(id(Items.WHEAT), food(1.0F, 4.0F));

        builder.addTag("c:crops/potato", 145, dish(null, 1.5F));
        builder.add(id(Items.POTATO), dish(null, 1.5F));
        builder.add(id(Items.BAKED_POTATO), dish(null, 2.5F));

        builder.addTag("c:crops/beetroot", 145, effect(2.0F, 1.0F, "strength", MEDIUM));
        builder.add(id(Items.BEETROOT), effect(2.0F, 1.0F, "strength", MEDIUM));

        builder.addTag("c:crops/rice", 145, dish(null, 1.0F));

        builder.add(id(Items.CARROT), effect(null, 1.5F, "night_vision", MEDIUM));
        builder.add(id(Items.GOLDEN_CARROT), build(null, 4.0F, 0,
                Optional.of(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "night_vision")), STRONG,
                0.0F, 2.0F, 0));

        builder.add(id(Items.PUMPKIN), effect(null, 2.0F, "resistance", MEDIUM));
        builder.add(id(Items.TURTLE_SCUTE), effect(0.0F, 0.0F, "resistance", MEDIUM));
        builder.addTag("c:crops/tomato", 145, effect(null, 2.0F, "strength", MEDIUM));
        builder.addTag("c:crops/onion", 145, effect(1.0F, 1.0F, "strength", WEAK));
        builder.addTag("c:crops/cabbage", 145, food(1.0F, 2.0F));

        builder.add(id(Items.DRIED_KELP), effect(null, 1.0F, "water_breathing", MEDIUM));
        builder.add(id(Items.KELP), effect(0.5F, 0.5F, "water_breathing", WEAK));
    }

    private void proteins(Builder builder) {
        builder.addTag("c:foods/raw_meat", 140, food(2.0F, 1.0F));
        builder.addTag("c:foods/cooked_meat", 145, food(5.0F, 2.0F));
        builder.addTag("c:foods/raw_fish", 140, dish(3.0F, null, 120));
        builder.addTag("c:foods/cooked_fish", 145, dish(5.0F, null, 150));

        builder.add(id(Items.BEEF), food(2.0F, 1.0F));
        builder.add(id(Items.COOKED_BEEF), food(6.0F, 2.0F));
        builder.add(id(Items.PORKCHOP), food(2.0F, 1.0F));
        builder.add(id(Items.COOKED_PORKCHOP), food(6.0F, 2.0F));
        builder.add(id(Items.MUTTON), food(2.0F, 1.0F));
        builder.add(id(Items.COOKED_MUTTON), food(5.0F, 2.0F));
        builder.add(id(Items.CHICKEN), dish(1.5F, null, 150));
        builder.add(id(Items.COOKED_CHICKEN), food(4.0F, 2.0F));
        builder.add(id(Items.RABBIT), food(1.0F, 1.0F));
        builder.add(id(Items.COOKED_RABBIT), food(4.0F, 2.0F));

        builder.add(id(Items.SALMON), temporary(4.0F, null, 120, 1.0F, 0.0F));
        builder.add(id(Items.COOKED_SALMON), temporary(6.0F, null, 180, 2.0F, 0.0F));
        builder.add(id(Items.COD), dish(3.0F, null, 90));
        builder.add(id(Items.COOKED_COD), dish(5.0F, null, 150));
        builder.add(id(Items.TROPICAL_FISH), effect(0.5F, 0.5F, "water_breathing", WEAK));

        builder.add(id(Items.EGG), food(0.5F, 1.0F));
    }

    private void fruitsAndSweets(Builder builder) {
        builder.add(id(Items.SUGAR), effect(0.0F, 1.0F, "speed", MEDIUM));
        builder.add(id(Items.HONEY_BOTTLE), effect(2.0F, 2.0F, "speed", MEDIUM));
        builder.add(id(Items.HONEYCOMB), effect(null, 2.0F, "speed", WEAK));

        builder.add(id(Items.COCOA_BEANS), effect(0.5F, 1.0F, "haste", MEDIUM));
        builder.add(id(Items.AMETHYST_SHARD), effect(0.0F, 0.0F, "haste", MEDIUM));

        builder.add(id(Items.MELON_SLICE), effect(null, 1.0F, "fire_resistance", MEDIUM));
        builder.add(id(Items.GLOW_BERRIES), effect(1.0F, 1.0F, "glowing", MEDIUM));

        builder.add(id(Items.SWEET_BERRIES), temporary(null, 1.0F, 0, 1.0F, 0.0F));
        builder.add(id(Items.APPLE), dish(2.0F, null, 100));
        builder.add(id(Items.GOLDEN_APPLE), temporary(4.0F, 4.0F, 0, 4.0F, 4.0F));
        builder.add(id(Items.ENCHANTED_GOLDEN_APPLE), temporary(8.0F, 8.0F, 0, 8.0F, 8.0F));

        builder.add(id(Items.CHORUS_FRUIT), effect(2.0F, 1.0F, "jump_boost", MEDIUM));
        builder.add(id(Items.PHANTOM_MEMBRANE), effect(0.0F, 0.0F, "jump_boost", MEDIUM));
    }

    private void staples(Builder builder) {
        builder.add(id(Items.SUGAR_CANE), timeBoost(0.0F, 0.5F, 50));
        builder.add(id(Items.MILK_BUCKET), timeBoost(1.0F, 1.0F, 50));
        builder.addTag("c:drinks/milk", 140, timeBoost(1.0F, 1.0F, 50));
        builder.addTag("c:foods/dough", 140, timeBoost(1.0F, 2.0F, 30));
        builder.addTag("c:foods/pasta", 140, timeBoost(1.0F, 3.0F, 30));
        builder.addTag("c:foods/bread", 130, timeBoost(3.0F, 5.0F, 30));
    }

    private void misc(Builder builder) {
        builder.add(id(Items.BROWN_MUSHROOM), food(0.5F, 1.0F));
        builder.add(id(Items.RED_MUSHROOM), food(0.5F, 0.5F));
        builder.add(id(Items.WARPED_FUNGUS), effect(0.5F, 0.5F, "invisibility", MEDIUM));
        builder.add(id(Items.FERMENTED_SPIDER_EYE), effect(0.0F, 0.0F, "invisibility", MEDIUM));
        builder.add(id(Items.CRIMSON_FUNGUS), effect(0.5F, 0.5F, "fire_resistance", WEAK));

        builder.add(id(Items.MAGMA_CREAM), effect(0.0F, 0.5F, "fire_resistance", STRONG));
        builder.add(id(Items.BLAZE_POWDER), effect(0.0F, 0.5F, "fire_resistance", MEDIUM));

        builder.add(id(Items.PUFFERFISH), effect(0.5F, 0.5F, "water_breathing", STRONG));
        builder.add(id(Items.NAUTILUS_SHELL), effect(0.0F, 0.0F, "water_breathing", MEDIUM));

        builder.add(id(Items.RABBIT_FOOT), effect(0.0F, 0.0F, "luck", STRONG));
        builder.add(id(Items.SPIDER_EYE), food(0.5F, 0.0F));
        builder.add(id(Items.ROTTEN_FLESH), food(0.5F, 0.0F));
    }

    private void dishes(Builder builder) {
        builder.add("farmersdelight:apple_cider", dish(2F, null, 100));
        builder.add("farmersdelight:chicken_cuts", dish(1F, null, 150));
        builder.add("farmersdelight:cod_slice", dish(2F, null, 90));
        builder.add("farmersdelight:cooked_cod_slice", dish(3F, null, 130));
        builder.add("farmersdelight:cooked_salmon_slice", dish(3.75F, null, 145));
        builder.add("farmersdelight:pie_crust", dish(null, 1.5F));
        builder.add("farmersdelight:pumpkin_slice", dish(null, 1F));
        builder.add("farmersdelight:rice", dish(null, 1F));
        builder.add("farmersdelight:salmon_slice", dish(2.5F, null, 100));
        builder.add("farmersdelight:tomato", dish(null, 2F));
        builder.add("farmersdelight:tomato_sauce", dish(null, 2.5F));
        builder.add(id(Items.GLISTERING_MELON_SLICE), build(4F, null, 120,
                Optional.empty(), 0, 0.0F, 1.0F, 0));
    }

    @Override
    public String getName() {
        return "Modest Meals ingredient profiles";
    }

    private record Entry(String target, int priority, IngredientProfile profile) {
    }

    private static final class Builder {
        private final List<Entry> entries = new ArrayList<>();

        Builder add(String target, IngredientProfile profile) {
            entries.add(new Entry(target, 0, profile));
            return this;
        }

        Builder addTag(String tag, int priority, IngredientProfile profile) {
            entries.add(new Entry("#" + tag, priority, profile));
            return this;
        }
    }
}
