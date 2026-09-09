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
        return build(health, stamina, 0, Optional.empty(), 0, 0.0F, 0);
    }

    private static IngredientProfile effect(Float health, Float stamina, String axis, int potency) {
        return build(health, stamina, 0,
                Optional.of(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, axis)), potency,
                0.0F, 0);
    }

    private static IngredientProfile temporary(Float health, Float stamina, int digestTicks,
                                               float temporaryHealth) {
        return build(health, stamina, digestTicks, Optional.empty(), 0,
                temporaryHealth, 0);
    }

    private static IngredientProfile timeBoost(Float health, Float stamina, int seconds) {
        return build(health, stamina, 0, Optional.empty(), 0, 0.0F, seconds);
    }

    private static IngredientProfile dish(Float health, Float stamina) {
        return dish(health, stamina, 0);
    }

    private static IngredientProfile dish(Float health, Float stamina, int digestTicks) {
        return build(health, stamina, digestTicks, Optional.empty(), 0, 0.0F, 0);
    }

    private static IngredientProfile build(Float health, Float stamina, int digestTicks,
                                           Optional<ResourceLocation> effect, int potency,
                                           float temporaryHealth, int timeBonus) {
        return new IngredientProfile(
                Optional.ofNullable(health), Optional.ofNullable(stamina),
                digestTicks > 0 ? Optional.of(digestTicks) : Optional.empty(),
                Optional.empty(),
                effect, potency, temporaryHealth, timeBonus);
    }

    private static String id(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    private static String cd(String path) {
        return "culturaldelights:" + path;
    }

    private static String cc(String path) {
        return "cookscollection:" + path;
    }

    private static String md(String path) {
        return "minersdelight:" + path;
    }

    private static String mnd(String path) {
        return "mynethersdelight:" + path;
    }

    private static String nml(String path) {
        return "nomansland:" + path;
    }

    private static String nmd(String path) {
        return "nomansdelight:" + path;
    }

    private static String ad(String path) {
        return "abnormals_delight:" + path;
    }

    private static String env(String path) {
        return "environmental:" + path;
    }

    private static String neo(String path) {
        return "neapolitan:" + path;
    }

    private static String bnc(String path) {
        return "brewinandchewin:" + path;
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
        culturalDelights(files.computeIfAbsent("cultural_delights", key -> new Builder()));
        cooksCollection(files.computeIfAbsent("cooks_collection", key -> new Builder()));
        minersDelight(files.computeIfAbsent("miners_delight", key -> new Builder()));
        myNethersDelight(files.computeIfAbsent("my_nethers_delight", key -> new Builder()));
        noMansLand(files.computeIfAbsent("no_mans_land", key -> new Builder()));
        noMansDelight(files.computeIfAbsent("nomansdelight", key -> new Builder()));
        abnormalsDelight(files.computeIfAbsent("abnormals_delight", key -> new Builder()));
        environmental(files.computeIfAbsent("environmental", key -> new Builder()));
        neapolitan(files.computeIfAbsent("neapolitan", key -> new Builder()));
        brewinAndChewin(files.computeIfAbsent("brewin_and_chewin", key -> new Builder()));

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
                0.0F, 0));

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

        builder.add(id(Items.SALMON), temporary(4.0F, null, 120, 1.0F));
        builder.add(id(Items.COOKED_SALMON), temporary(6.0F, null, 180, 2.0F));
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

        builder.add(id(Items.SWEET_BERRIES), temporary(null, 1.0F, 0, 1.0F));
        builder.add(id(Items.APPLE), dish(2.0F, null, 100));
        builder.add(id(Items.GOLDEN_APPLE), temporary(4.0F, 4.0F, 0, 4.0F));
        builder.add(id(Items.ENCHANTED_GOLDEN_APPLE), temporary(8.0F, 8.0F, 0, 8.0F));

        builder.add(id(Items.CHORUS_FRUIT), effect(2.0F, 1.0F, "jump_boost", MEDIUM));
        builder.add(id(Items.PHANTOM_MEMBRANE), effect(0.0F, 0.0F, "jump_boost", MEDIUM));
    }

    private void staples(Builder builder) {
        builder.add(id(Items.SUGAR_CANE), timeBoost(0.0F, 0.5F, 50));
        builder.add(id(Items.MILK_BUCKET), timeBoost(1.0F, 1.0F, 50));
        builder.addTag("c:drinks/milk", 140, timeBoost(1.0F, 1.0F, 50));
        builder.addTag("c:foods/dough", 140, timeBoost(1.0F, 2.0F, 30));
        builder.addTag("c:foods/pasta", 140, timeBoost(1.0F, 3.0F, 30));
        builder.addTag("c:foods/tortilla", 140, timeBoost(2.0F, 3.0F, 30));
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

        builder.add(id(Items.BLAZE_ROD), effect(0.0F, 0.5F, "cold_resistance", STRONG));
        builder.add(id(Items.NETHER_WART), effect(0.0F, 0.5F, "cold_resistance", MEDIUM));

        builder.add(id(Items.BLUE_ICE), effect(0.0F, 0.5F, "heat_resistance", STRONG));
        builder.add(id(Items.SNOWBALL), effect(0.0F, 0.5F, "heat_resistance", MEDIUM));

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
                Optional.empty(), 0, 0.0F, 0));
    }

    private void culturalDelights(Builder builder) {
        builder.addTag("c:crops/avocado", 145, food(2.0F, 2.0F));
        builder.add(cd("avocado"), food(2.0F, 2.0F));
        builder.add(cd("cut_avocado"), food(1.0F, 1.0F));

        builder.addTag("c:crops/cucumber", 145, food(0.5F, 1.5F));
        builder.add(cd("cucumber"), food(0.5F, 1.5F));
        builder.add(cd("cut_cucumber"), food(0.5F, 0.5F));
        builder.add(cd("pickle"), effect(1.0F, 2.0F, "speed", WEAK));
        builder.add(cd("cut_pickle"), effect(0.5F, 1.0F, "speed", WEAK));

        builder.addTag("c:crops/eggplant", 145, food(1.0F, 1.5F));
        builder.add(cd("eggplant"), food(1.0F, 1.5F));
        builder.add(cd("cut_eggplant"), food(0.5F, 0.5F));
        builder.add(cd("white_eggplant"), food(0.5F, 0.5F));
        builder.add(cd("smoked_eggplant"), food(3.0F, 2.5F));
        builder.add(cd("smoked_cut_eggplant"), food(1.5F, 1.0F));
        builder.add(cd("smoked_white_eggplant"), food(1.0F, 1.0F));
        builder.add(cd("smoked_tomato"), effect(1.0F, 2.5F, "strength", MEDIUM));

        builder.addTag("c:crops/corn", 145, food(0.5F, 2.0F));
        builder.add(cd("corn_cob"), food(0.5F, 2.0F));
        builder.add(cd("popcorn"), food(null, 1.0F));
        builder.add(cd("tortilla_chips"), food(0.5F, 1.5F));
        builder.add(cd("corn_dough"), timeBoost(1.0F, 2.0F, 30));

        builder.add(cd("ginger"), effect(0.5F, 1.0F, "cold_resistance", MEDIUM));

        builder.add(cd("squid"), dish(1.0F, null, 120));
        builder.add(cd("cooked_squid"), food(3.0F, 2.0F));
        builder.add(cd("glow_squid"), effect(1.0F, 0.5F, "glowing", MEDIUM));
        builder.add(cd("raw_calamari"), dish(0.5F, null, 110));
        builder.add(cd("cooked_calamari"), food(1.5F, 1.0F));
    }

    private void cooksCollection(Builder builder) {
        builder.addTag("c:crops/lemon", 145, effect(0.5F, 1.5F, "speed", WEAK));
        builder.add(cc("lemon"), effect(0.5F, 1.5F, "speed", WEAK));

        builder.add(cc("salt"), timeBoost(0.0F, 0.5F, 60));
        builder.add(cc("cooking_oil"), timeBoost(0.5F, 1.0F, 40));

        builder.add(cc("fried_potato"), dish(null, 1.5F));
        builder.add(cc("rustic_loaf_slice"), timeBoost(1.0F, 2.0F, 25));
    }

    private void minersDelight(Builder builder) {
        builder.addTag("c:crops/cave_carrot", 145, effect(null, 1.5F, "night_vision", WEAK));
        builder.add(md("cave_carrot"), effect(null, 1.5F, "night_vision", WEAK));
        builder.add(md("baked_cave_carrot"), effect(null, 2.5F, "night_vision", MEDIUM));
        builder.add(md("copper_carrot"), effect(0.5F, 1.5F, "haste", MEDIUM));

        builder.add(md("moss"), food(0.5F, 0.5F));
        builder.add(md("vegan_patty"), food(1.0F, 2.0F));

        builder.add(md("bat_wing"), dish(1.0F, null, 120));
        builder.add(md("smoked_bat_wing"), food(2.0F, 1.0F));

        builder.addTag("minersdelight:raw_insect_meat", 140, dish(1.0F, null, 120));
        builder.addTag("minersdelight:cooked_insect_meat", 145, food(2.5F, 1.5F));
        builder.add(md("spider_leg"), food(1.0F, 0.5F));
        builder.add(md("baked_spider_leg"), food(3.0F, 1.0F));
        builder.add(md("arthropod"), dish(1.0F, null, 120));
        builder.add(md("cooked_arthropod"), food(2.5F, 1.5F));
        builder.add(md("silverfish_eggs"), food(0.5F, 0.5F));

        builder.addTag("c:foods/raw_squid", 140, dish(1.0F, null, 120));
        builder.addTag("c:foods/cooked_squid", 145, food(3.0F, 2.0F));
        builder.add(md("squid"), dish(1.0F, null, 120));
        builder.add(md("baked_squid"), food(3.0F, 2.0F));
        builder.add(md("glow_squid"), effect(1.0F, 0.5F, "glowing", MEDIUM));
        builder.add(md("tentacles"), dish(0.5F, null, 110));
        builder.add(md("baked_tentacles"), food(1.5F, 1.0F));
    }

    private void myNethersDelight(Builder builder) {
        builder.add(mnd("bullet_pepper"), effect(0.5F, 1.0F, "fire_resistance", MEDIUM));
        builder.add(mnd("pepper_powder"), effect(0.0F, 0.5F, "fire_resistance", MEDIUM));

        builder.addTag("c:foods/raw_hoglin", 140, food(2.0F, 1.0F));
        builder.addTag("c:foods/cooked_hoglin", 145, food(6.0F, 2.0F));
        builder.add(mnd("hoglin_loin"), food(2.0F, 1.0F));
        builder.add(mnd("cooked_loin"), food(6.0F, 2.0F));
        builder.addTag("c:foods/raw_sausage", 140, food(1.0F, 1.0F));
        builder.addTag("c:foods/cooked_sausage", 145, food(3.0F, 1.5F));

        builder.addTag("c:foods/raw_strider", 140, effect(2.0F, 1.0F, "fire_resistance", WEAK));
        builder.add(mnd("strider_slice"), effect(2.0F, 1.0F, "fire_resistance", WEAK));
        builder.add(mnd("minced_strider"), food(1.0F, 1.0F));
        builder.add(mnd("strider_egg"), effect(1.0F, 1.5F, "fire_resistance", WEAK));

        builder.addTag("c:foods/boiled_egg", 145, food(1.5F, 2.0F));
        builder.add(mnd("boiled_egg"), food(1.5F, 2.0F));
        builder.add(mnd("golden_egg"), effect(2.0F, 3.0F, "resistance", MEDIUM));
        builder.add(mnd("enchanted_golden_egg"), effect(4.0F, 4.0F, "resistance", STRONG));

        builder.add(mnd("ghast_sourdough"), timeBoost(1.0F, 2.0F, 30));
        builder.add(mnd("roast_ear"), food(1.0F, 0.5F));
    }

    private void noMansLand(Builder builder) {
        builder.addTag("c:foods/raw_venison", 140, food(2.0F, 1.0F));
        builder.addTag("c:foods/cooked_venison", 145, food(5.0F, 2.0F));
        builder.addTag("c:foods/raw_horse", 140, food(2.0F, 1.0F));
        builder.addTag("c:foods/cooked_horse", 145, food(5.0F, 2.0F));
        builder.addTag("c:foods/raw_billhook_bass", 140, dish(3.0F, null, 120));
        builder.addTag("c:foods/cooked_billhook_bass", 145, dish(5.0F, null, 150));

        builder.add(nml("frog_leg"), food(1.5F, 1.0F));
        builder.add(nml("cooked_frog_leg"), food(4.0F, 2.0F));
        builder.add(nml("field_mushroom"), food(0.5F, 1.0F));
        builder.add(nml("grilled_mushrooms"), food(1.5F, 1.5F));

        builder.addTag("c:foods/pear", 145, dish(2.0F, null, 100));
        builder.add(nml("pear"), dish(2.0F, null, 100));
        builder.add(nml("pear_juice"), dish(2.0F, null, 100));

        builder.add(nml("pine_nuts"), food(1.0F, 1.5F));
        builder.add(nml("walnuts"), food(1.0F, 1.5F));

        builder.add(nml("maple_syrup_bottle"), effect(1.0F, 2.0F, "speed", MEDIUM));
        builder.add(nml("pesto_bottle"), effect(1.0F, 2.0F, "strength", WEAK));
    }

    private void noMansDelight(Builder builder) {
        builder.add(nmd("horse_cuts"), food(2.0F, 1.0F));
        builder.add(nmd("cooked_horse_cuts"), food(4.0F, 1.5F));
        builder.add(nmd("venison_chop"), food(1.5F, 1.0F));
        builder.add(nmd("cooked_venison_chop"), food(3.5F, 1.5F));
        builder.add(nmd("billhook_bass_slice"), dish(1.5F, null, 100));
        builder.add(nmd("cooked_billhook_bass_slice"), dish(3.0F, null, 130));

    }

    private void abnormalsDelight(Builder builder) {
        builder.addTag("c:foods/raw_duck", 140, dish(1.5F, null, 150));
        builder.addTag("c:foods/cooked_duck", 145, food(5.0F, 2.0F));
        builder.add(ad("duck_fillet"), dish(1.5F, null, 130));
        builder.add(ad("cooked_duck_fillet"), food(3.0F, 1.5F));

        builder.add(ad("venison_shanks"), food(1.0F, 0.5F));
        builder.add(ad("cooked_venison_shanks"), food(2.5F, 1.5F));

        builder.addTag("c:foods/raw_pike", 140, dish(1.5F, null, 100));
        builder.addTag("c:foods/cooked_pike", 145, dish(3.0F, null, 130));
        builder.addTag("c:foods/raw_perch", 140, dish(1.0F, null, 90));
        builder.addTag("c:foods/cooked_perch", 145, dish(2.0F, null, 120));
        builder.add(ad("pike_slice"), dish(1.5F, null, 100));
        builder.add(ad("cooked_pike_slice"), dish(3.0F, null, 130));
        builder.add(ad("perch_slice"), dish(1.0F, null, 90));
        builder.add(ad("cooked_perch_slice"), dish(2.0F, null, 120));
    }

    private void environmental(Builder builder) {
        builder.addTag("c:foods/cherry", 145, dish(1.0F, null, 60));
        builder.add(env("cherries"), dish(1.0F, null, 60));
        builder.addTag("c:foods/plum", 145, dish(2.5F, null, 110));
        builder.add(env("plum"), dish(2.5F, null, 110));

        builder.add(env("venison"), food(2.0F, 1.0F));
        builder.add(env("cooked_venison"), food(6.0F, 2.0F));
        builder.add(env("duck"), dish(1.5F, null, 150));
        builder.add(env("cooked_duck"), food(5.0F, 2.0F));
        builder.add(env("duck_egg"), food(0.5F, 1.0F));

        builder.add(env("koi"), effect(0.5F, 0.5F, "luck", WEAK));
        builder.add(env("truffle"), effect(3.0F, 4.0F, "luck", MEDIUM));
    }

    private void neapolitan(Builder builder) {
        builder.addTag("c:foods/strawberry", 145, temporary(null, 1.5F, 0, 1.0F));
        builder.add(neo("strawberries"), temporary(null, 1.5F, 0, 1.0F));
        builder.add(neo("white_strawberries"), temporary(null, 2.0F, 0, 2.0F));

        builder.addTag("c:foods/banana", 145, effect(1.0F, 2.0F, "climbing", MEDIUM));
        builder.add(neo("banana"), effect(1.0F, 2.0F, "climbing", MEDIUM));
        builder.add(neo("dried_banana"), effect(1.5F, 2.5F, "climbing", MEDIUM));

        builder.addTag("c:crops/mango", 145, dish(1.5F, null, 80));
        builder.add(neo("mango"), dish(1.5F, null, 80));
        builder.add(neo("dried_mango"), dish(2.0F, null, 100));
        builder.add(neo("mango_fish"), dish(2.0F, null, 110));
        builder.add(neo("cooked_mango_fish"), dish(4.0F, null, 140));

        builder.add(neo("mint_leaves"), effect(0.5F, 1.5F, "heat_resistance", MEDIUM));

        builder.add(neo("cinnamon_sticks"), effect(0.5F, 1.0F, "cold_resistance", MEDIUM));
        builder.add(neo("ice_cubes"), effect(0.0F, 0.5F, "heat_resistance", MEDIUM));

        builder.add(neo("adzuki_beans"), effect(0.5F, 1.5F, "harmony", WEAK));
        builder.add(neo("roasted_adzuki_beans"), effect(1.5F, 2.0F, "harmony", MEDIUM));

        builder.addTag("c:foods/chocolate_bar", 145, effect(1.0F, 2.0F, "haste", MEDIUM));
        builder.add(neo("chocolate_bar"), effect(1.0F, 2.0F, "haste", MEDIUM));
        builder.add(neo("chocolate_spider_eye"), effect(0.5F, 1.0F, "invisibility", MEDIUM));

        builder.add(neo("vanilla_pods"), food(0.5F, 0.5F));
        builder.add(neo("dried_vanilla_pods"), effect(0.5F, 1.0F, "luck", WEAK));
        builder.add(neo("waffle_cone"), timeBoost(0.5F, 1.5F, 25));
    }

    private void brewinAndChewin(Builder builder) {
        builder.addTag("brewinandchewin:grapes", 145, dish(1.5F, null, 80));
        builder.add(bnc("red_grapes"), dish(1.5F, null, 80));
        builder.add(bnc("white_grapes"), dish(1.5F, null, 80));

        builder.add(bnc("cooked_corn"), food(1.5F, 2.5F));
        builder.add(bnc("popped_corn"), food(null, 1.0F));
        builder.add(bnc("cornmeal"), timeBoost(0.5F, 1.5F, 30));

        builder.addTag("brewinandchewin:foods/cheese_wedge", 145, food(2.0F, 3.0F));
        builder.add(bnc("rennet"), food(0.5F, 0.5F));

        builder.add(bnc("innards"), food(0.5F, 0.5F));
        builder.add(bnc("jerky"), food(2.0F, 1.5F));
        builder.add(bnc("aspic_cube"), food(2.0F, 1.5F));
        builder.add(bnc("kippers"), dish(3.0F, null, 130));

        builder.add(bnc("kimchi"), effect(1.0F, 2.0F, "speed", WEAK));
        builder.add(bnc("pickled_pickles"), effect(1.0F, 2.0F, "speed", WEAK));

        builder.add(bnc("sweet_berry_jam"), temporary(null, 2.0F, 0, 1.0F));
        builder.add(bnc("glow_berry_marmalade"), effect(1.0F, 2.0F, "glowing", MEDIUM));
        builder.add(bnc("apple_jelly"), dish(3.0F, null, 120));
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
