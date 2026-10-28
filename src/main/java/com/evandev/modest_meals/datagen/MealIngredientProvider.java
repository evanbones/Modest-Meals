package com.evandev.modest_meals.datagen;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.ingredient.MealLayerVisual;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class MealIngredientProvider extends JsonDataProvider<MealIngredientProvider.Entry> {

    private static final ResourceLocation SANDWICH_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sandwich");
    private static final ResourceLocation WRAP_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "wrap");
    private static final ResourceLocation PIZZA_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "pizza");
    private static final ResourceLocation PIZZA_SLICE_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "pizza_slice");
    private static final ResourceLocation PLACED_PIZZA_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "placed_pizza");
    private static final int PIZZA_SLOTS = 5;
    private static final ResourceLocation ICE_CREAM_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "ice_cream");

    public MealIngredientProvider(PackOutput output) {
        super(output, "modest_meals/meal_ingredients", Entry.CODEC, "Modest Meals meal ingredients");
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
    }

    private static ResourceLocation sandwichSprite(String name) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/sandwich/" + name);
    }

    private static ResourceLocation wrapSprite(String name) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/wrap/" + name);
    }

    private static MealLayerVisual pizzaToppings(String folder, String name) {
        ResourceLocation base = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/" + folder + "/" + name);
        return MealLayerVisual.slots(IntStream.range(0, PIZZA_SLOTS).mapToObj(i -> base.withSuffix("_" + i)).toList());
    }

    private static Entry sandwich(String item, String baseSprite) {
        return new Entry(item, Map.of(
                SANDWICH_ID, MealLayerVisual.triplet(sandwichSprite(baseSprite)),
                WRAP_ID, MealLayerVisual.triplet(wrapSprite(baseSprite))
        ));
    }

    private static Entry topping(String item, String baseSprite) {
        return new Entry(item, Map.of(
                SANDWICH_ID, MealLayerVisual.triplet(sandwichSprite(baseSprite)),
                WRAP_ID, MealLayerVisual.triplet(wrapSprite(baseSprite)),
                PIZZA_ID, pizzaToppings("pizza", baseSprite),
                PIZZA_SLICE_ID, pizzaToppings("pizza_slice", baseSprite),
                PLACED_PIZZA_ID, pizzaToppings("placed_pizza", baseSprite)
        ));
    }

    private static Entry pizza(String item, String toppingSprite) {
        return new Entry(item, Map.of(
                PIZZA_ID, pizzaToppings("pizza", toppingSprite),
                PIZZA_SLICE_ID, pizzaToppings("pizza_slice", toppingSprite),
                PLACED_PIZZA_ID, pizzaToppings("placed_pizza", toppingSprite)
        ));
    }

    private static MealLayerVisual iceCream(String flavour) {
        return MealLayerVisual.triplet(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/ice_cream/" + flavour));
    }

    public static Builder builder(String item) {
        return new Builder(item);
    }

    @Override
    protected void collect(Map<ResourceLocation, Entry> entries) {
        entries.put(id("apple"), sandwich("minecraft:apple", "apple"));
        entries.put(id("baked_potato"), topping("minecraft:baked_potato", "baked_potato"));
        entries.put(id("beetroot"), sandwich("minecraft:beetroot", "beetroot"));
        entries.put(id("carrot"), sandwich("minecraft:carrot", "carrot"));
        entries.put(id("chorus_fruit"), sandwich("minecraft:chorus_fruit", "chorus_fruit"));
        entries.put(id("cocoa_beans"), sandwich("minecraft:cocoa_beans", "cocoa_beans"));
        entries.put(id("glow_berries"), sandwich("minecraft:glow_berries", "glow_berries"));
        entries.put(id("honey_bottle"), sandwich("minecraft:honey_bottle", "honey"));
        entries.put(id("jam"), sandwich("modest_meals:jam", "sweet_berries"));
        entries.put(id("kelp"), sandwich("minecraft:kelp", "kelp"));
        entries.put(id("dried_kelp"), sandwich("minecraft:dried_kelp", "kelp"));
        entries.put(id("melon_slice"), sandwich("minecraft:melon_slice", "melon"));
        entries.put(id("sweet_berries"), sandwich("minecraft:sweet_berries", "sweet_berries"));
        entries.put(id("rotten_flesh"), sandwich("minecraft:rotten_flesh", "rotten_flesh"));
        entries.put(id("spider_eye"), sandwich("minecraft:spider_eye", "spider_eye"));
        entries.put(id("brown_mushroom"), pizza("minecraft:brown_mushroom", "mushroom"));
        entries.put(id("red_mushroom"), pizza("minecraft:red_mushroom", "mushroom"));
        entries.put(id("egg"), pizza("minecraft:egg", "fried_egg"));

        entries.put(id("cooked_beef"), topping("minecraft:cooked_beef", "beef"));
        entries.put(id("beef"), topping("minecraft:beef", "beef"));
        entries.put(id("cooked_chicken"), topping("minecraft:cooked_chicken", "chicken"));
        entries.put(id("chicken"), topping("minecraft:chicken", "chicken"));
        entries.put(id("cooked_porkchop"), topping("minecraft:cooked_porkchop", "pork"));
        entries.put(id("porkchop"), topping("minecraft:porkchop", "pork"));
        entries.put(id("cooked_mutton"), topping("minecraft:cooked_mutton", "mutton"));
        entries.put(id("mutton"), topping("minecraft:mutton", "mutton"));
        entries.put(id("cooked_rabbit"), topping("minecraft:cooked_rabbit", "rabbit"));
        entries.put(id("rabbit"), topping("minecraft:rabbit", "rabbit"));
        entries.put(id("cooked_cod"), topping("minecraft:cooked_cod", "cod"));
        entries.put(id("cod"), topping("minecraft:cod", "cod"));
        entries.put(id("cooked_salmon"), topping("minecraft:cooked_salmon", "salmon"));
        entries.put(id("salmon"), topping("minecraft:salmon", "salmon"));

        entries.put(id("cabbage"), topping("farmersdelight:cabbage", "cabbage"));
        entries.put(id("cabbage_leaf"), topping("farmersdelight:cabbage_leaf", "cabbage"));
        entries.put(id("onion"), topping("farmersdelight:onion", "onion"));
        entries.put(id("tomato"), topping("farmersdelight:tomato", "tomato"));
        entries.put(id("fried_egg"), topping("farmersdelight:fried_egg", "fried_egg"));
        entries.put(id("cooked_rice"), sandwich("farmersdelight:cooked_rice", "cooked_rice"));
        entries.put(id("smoked_ham"), topping("farmersdelight:smoked_ham", "smoked_ham"));
        entries.put(id("ham"), topping("farmersdelight:ham", "smoked_ham"));
        entries.put(id("pumpkin_slice"), topping("farmersdelight:pumpkin_slice", "pumpkin"));

        entries.put(id("minced_beef"), topping("farmersdelight:minced_beef", "beef"));
        entries.put(id("beef_patty"), topping("farmersdelight:beef_patty", "beef"));
        entries.put(id("chicken_cuts"), topping("farmersdelight:chicken_cuts", "chicken"));
        entries.put(id("cooked_chicken_cuts"), topping("farmersdelight:cooked_chicken_cuts", "chicken"));
        entries.put(id("bacon"), topping("farmersdelight:bacon", "pork"));
        entries.put(id("cooked_bacon"), topping("farmersdelight:cooked_bacon", "pork"));
        entries.put(id("mutton_chops"), topping("farmersdelight:mutton_chops", "mutton"));
        entries.put(id("cooked_mutton_chops"), topping("farmersdelight:cooked_mutton_chops", "mutton"));
        entries.put(id("salmon_slice"), topping("farmersdelight:salmon_slice", "salmon"));
        entries.put(id("cooked_salmon_slice"), topping("farmersdelight:cooked_salmon_slice", "salmon"));
        entries.put(id("cod_slice"), topping("farmersdelight:cod_slice", "cod"));
        entries.put(id("cooked_cod_slice"), topping("farmersdelight:cooked_cod_slice", "cod"));

        entries.put(id("refried_beans"), sandwich("culturaldelights:refried_beans", "refried_beans"));
        entries.put(id("cinnamon"), sandwich("culturaldelights:cinnamon", "cinnamon"));
        entries.put(id("butter"), sandwich("culturaldelights:butter", "butter"));
        entries.put(id("cheese_wedge"), topping("culturaldelights:cheese_wedge", "cheese"));
        entries.put(id("cooked_sausage"), topping("culturaldelights:cooked_sausage", "sausage"));
        entries.put(id("lemon"), sandwich("culturaldelights:lemon", "lemon"));
        entries.put(id("fried_potato"), topping("culturaldelights:fried_potato", "baked_potato"));
        entries.put(id("raw_sausage"), topping("culturaldelights:raw_sausage", "sausage"));
        entries.put(id("popcorn"), sandwich("culturaldelights:popcorn", "popcorn"));
        entries.put(id("creamed_corn"), sandwich("culturaldelights:creamed_corn", "creamed_corn"));
        entries.put(id("tortilla_chips"), sandwich("culturaldelights:tortilla_chips", "tortilla_chips"));

        entries.put(id("avocado"), sandwich("culturaldelights:avocado", "avocado"));
        entries.put(id("cut_avocado"), sandwich("culturaldelights:cut_avocado", "avocado"));
        entries.put(id("cucumber"), sandwich("culturaldelights:cucumber", "cucumber"));
        entries.put(id("cut_cucumber"), sandwich("culturaldelights:cut_cucumber", "cucumber"));
        entries.put(id("pickle"), sandwich("culturaldelights:pickle", "pickle"));
        entries.put(id("cut_pickle"), sandwich("culturaldelights:cut_pickle", "pickle"));
        entries.put(id("eggplant"), topping("culturaldelights:eggplant", "eggplant"));
        entries.put(id("cut_eggplant"), topping("culturaldelights:cut_eggplant", "eggplant"));
        entries.put(id("corn_cob"), pizza("culturaldelights:corn_cob", "creamed_corn"));
        entries.put(id("smoked_eggplant"), topping("culturaldelights:smoked_eggplant", "eggplant"));
        entries.put(id("white_eggplant"), topping("culturaldelights:white_eggplant", "eggplant"));
        entries.put(id("smoked_cut_eggplant"), topping("culturaldelights:smoked_cut_eggplant", "eggplant"));
        entries.put(id("smoked_white_eggplant"), topping("culturaldelights:smoked_white_eggplant", "eggplant"));
        entries.put(id("smoked_tomato"), topping("culturaldelights:smoked_tomato", "tomato"));

        entries.put(id("squid"), topping("culturaldelights:squid", "squid"));
        entries.put(id("cooked_squid"), topping("culturaldelights:cooked_squid", "squid"));
        entries.put(id("glow_squid"), topping("culturaldelights:glow_squid", "squid"));
        entries.put(id("raw_calamari"), topping("culturaldelights:raw_calamari", "squid"));
        entries.put(id("cooked_calamari"), topping("culturaldelights:cooked_calamari", "squid"));

        entries.put(id("duck"), topping("environmental:duck", "chicken"));
        entries.put(id("cooked_duck"), topping("environmental:cooked_duck", "chicken"));
        entries.put(id("venison"), topping("environmental:venison", "beef"));
        entries.put(id("cooked_venison"), topping("environmental:cooked_venison", "beef"));
        entries.put(id("env_cherries"), sandwich("environmental:cherries", "cherry"));
        entries.put(id("env_plum"), sandwich("environmental:plum", "plum"));
        entries.put(id("env_truffle"), topping("environmental:truffle", "truffle"));
        entries.put(id("env_duck_egg"), topping("environmental:duck_egg", "fried_egg"));
        entries.put(id("env_koi"), sandwich("environmental:koi", "salmon"));

        entries.put(id("cinnamon_sticks"), sandwich("neapolitan:cinnamon_sticks", "cinnamon"));
        entries.put(id("mint_chops"), topping("neapolitan:mint_chops", "mutton"));
        entries.put(id("cooked_mint_chops"), topping("neapolitan:cooked_mint_chops", "mutton"));
        entries.put(id("mango_fish"), sandwich("neapolitan:mango_fish", "cod"));
        entries.put(id("cooked_mango_fish"), sandwich("neapolitan:cooked_mango_fish", "cod"));
        entries.put(id("neo_strawberries"), sandwich("neapolitan:strawberries", "strawberry"));
        entries.put(id("neo_white_strawberries"), sandwich("neapolitan:white_strawberries", "strawberry"));
        entries.put(id("neo_banana"), sandwich("neapolitan:banana", "banana"));
        entries.put(id("neo_dried_banana"), sandwich("neapolitan:dried_banana", "banana"));
        entries.put(id("neo_chocolate_bar"), sandwich("neapolitan:chocolate_bar", "chocolate"));
        entries.put(id("neo_vanilla_pods"), sandwich("neapolitan:vanilla_pods", "vanilla"));
        entries.put(id("neo_dried_vanilla_pods"), sandwich("neapolitan:dried_vanilla_pods", "vanilla"));
        entries.put(id("neo_adzuki_beans"), sandwich("neapolitan:adzuki_beans", "adzuki_beans"));
        entries.put(id("neo_roasted_adzuki_beans"), sandwich("neapolitan:roasted_adzuki_beans", "adzuki_beans"));
        entries.put(id("neo_mango"), sandwich("neapolitan:mango", "mango"));
        entries.put(id("neo_dried_mango"), sandwich("neapolitan:dried_mango", "mango"));
        entries.put(id("neo_mint_leaves"), sandwich("neapolitan:mint_leaves", "cabbage"));
        entries.put(id("neo_chocolate_spider_eye"), sandwich("neapolitan:chocolate_spider_eye", "spider_eye"));

        entries.put(id("cooked_egg"), topping("bountifulfares:cooked_egg", "fried_egg"));
        entries.put(id("popped_maize"), sandwich("bountifulfares:popped_maize", "popcorn"));
        entries.put(id("foul_flesh"), sandwich("bountifulfares:foul_flesh", "rotten_flesh"));
        entries.put(id("bf_orange"), sandwich("bountifulfares:orange", "orange"));
        entries.put(id("bf_lemon"), sandwich("bountifulfares:lemon", "lemon"));
        entries.put(id("bf_plum"), sandwich("bountifulfares:plum", "plum"));
        entries.put(id("bf_passion_fruit"), sandwich("bountifulfares:passion_fruit", "passion_fruit"));
        entries.put(id("bf_elderberries"), sandwich("bountifulfares:elderberries", "elderberry"));
        entries.put(id("bf_lapisberries"), sandwich("bountifulfares:lapisberries", "lapisberry"));
        entries.put(id("bf_walnut"), sandwich("bountifulfares:walnut", "walnut"));
        entries.put(id("bf_coconut_half"), sandwich("bountifulfares:coconut_half", "coconut"));
        entries.put(id("bf_maize"), topping("bountifulfares:maize", "creamed_corn"));
        entries.put(id("bf_leek"), topping("bountifulfares:leek", "onion"));
        entries.put(id("bf_hoary_apple"), sandwich("bountifulfares:hoary_apple", "apple"));
        entries.put(id("bf_spongekin_slice"), sandwich("bountifulfares:spongekin_slice", "pumpkin"));
        entries.put(id("bf_pickled_spongekin"), sandwich("bountifulfares:pickled_spongekin", "pickle"));
        entries.put(id("bf_pickled_beetroot"), sandwich("bountifulfares:pickled_beetroot", "beetroot"));

        entries.put(id("bnc_cheese_wedge"), topping("brewinandchewin:cheese_wedge", "cheese"));
        entries.put(id("bnc_cooked_corn"), topping("brewinandchewin:cooked_corn", "creamed_corn"));
        entries.put(id("bnc_popped_corn"), sandwich("brewinandchewin:popped_corn", "popcorn"));
        entries.put(id("bnc_jerky"), topping("brewinandchewin:jerky", "beef"));
        entries.put(id("bnc_kippers"), topping("brewinandchewin:kippers", "cod"));
        entries.put(id("bnc_pickled_pickles"), sandwich("brewinandchewin:pickled_pickles", "pickle"));
        entries.put(id("bnc_kimchi"), sandwich("brewinandchewin:kimchi", "cabbage"));
        entries.put(id("bnc_sweet_berry_jam"), sandwich("brewinandchewin:sweet_berry_jam", "sweet_berries"));
        entries.put(id("bnc_glow_berry_marmalade"), sandwich("brewinandchewin:glow_berry_marmalade", "glow_berries"));
        entries.put(id("bnc_apple_jelly"), sandwich("brewinandchewin:apple_jelly", "apple"));
        entries.put(id("bnc_innards"), sandwich("brewinandchewin:innards", "rotten_flesh"));
        entries.put(id("bnc_aspic_cube"), sandwich("brewinandchewin:aspic_cube", "pork"));
        entries.put(id("bnc_red_grapes"), sandwich("brewinandchewin:red_grapes", "red_grapes"));
        entries.put(id("bnc_white_grapes"), sandwich("brewinandchewin:white_grapes", "white_grapes"));

        entries.put(id("nml_raw_venison"), topping("nomansland:raw_venison", "beef"));
        entries.put(id("nml_cooked_venison"), topping("nomansland:cooked_venison", "beef"));
        entries.put(id("nml_raw_horse"), topping("nomansland:raw_horse", "beef"));
        entries.put(id("nml_cooked_horse"), topping("nomansland:cooked_horse", "beef"));
        entries.put(id("nml_horse_steak"), topping("nomansland:horse_steak", "beef"));
        entries.put(id("nml_raw_billhook_bass"), topping("nomansland:raw_billhook_bass", "salmon"));
        entries.put(id("nml_cooked_billhook_bass"), topping("nomansland:cooked_billhook_bass", "salmon"));
        entries.put(id("nml_billhook_bass"), topping("nomansland:billhook_bass", "salmon"));
        entries.put(id("nml_frog_leg"), topping("nomansland:frog_leg", "chicken"));
        entries.put(id("nml_cooked_frog_leg"), topping("nomansland:cooked_frog_leg", "chicken"));
        entries.put(id("nml_maple_syrup_bottle"), sandwich("nomansland:maple_syrup_bottle", "honey"));
        entries.put(id("nml_pine_nuts"), sandwich("nomansland:pine_nuts", "walnut"));
        entries.put(id("nml_walnuts"), sandwich("nomansland:walnuts", "walnut"));
        entries.put(id("nml_pear"), sandwich("nomansland:pear", "pear"));
        entries.put(id("nml_field_mushroom"), topping("nomansland:field_mushroom", "mushroom"));
        entries.put(id("nml_grilled_mushrooms"), topping("nomansland:grilled_mushrooms", "mushroom"));

        entries.put(id("nmd_horse_cuts"), topping("nomansdelight:horse_cuts", "beef"));
        entries.put(id("nmd_cooked_horse_cuts"), topping("nomansdelight:cooked_horse_cuts", "beef"));
        entries.put(id("nmd_venison_chop"), topping("nomansdelight:venison_chop", "beef"));
        entries.put(id("nmd_cooked_venison_chop"), topping("nomansdelight:cooked_venison_chop", "beef"));
        entries.put(id("nmd_billhook_bass_slice"), topping("nomansdelight:billhook_bass_slice", "salmon"));
        entries.put(id("nmd_cooked_billhook_bass_slice"), topping("nomansdelight:cooked_billhook_bass_slice", "salmon"));

        entries.put(id("ad_duck_fillet"), topping("abnormals_delight:duck_fillet", "chicken"));
        entries.put(id("ad_cooked_duck_fillet"), topping("abnormals_delight:cooked_duck_fillet", "chicken"));
        entries.put(id("ad_venison_shanks"), topping("abnormals_delight:venison_shanks", "beef"));
        entries.put(id("ad_cooked_venison_shanks"), topping("abnormals_delight:cooked_venison_shanks", "beef"));
        entries.put(id("ad_pike_slice"), topping("abnormals_delight:pike_slice", "cod"));
        entries.put(id("ad_cooked_pike_slice"), topping("abnormals_delight:cooked_pike_slice", "cod"));
        entries.put(id("ad_perch_slice"), topping("abnormals_delight:perch_slice", "salmon"));
        entries.put(id("ad_cooked_perch_slice"), topping("abnormals_delight:cooked_perch_slice", "salmon"));

        entries.put(id("md_vegan_patty"), topping("minersdelight:vegan_patty", "beef"));
        entries.put(id("md_cave_carrot"), sandwich("minersdelight:cave_carrot", "carrot"));
        entries.put(id("md_baked_cave_carrot"), sandwich("minersdelight:baked_cave_carrot", "carrot"));
        entries.put(id("md_copper_carrot"), sandwich("minersdelight:copper_carrot", "carrot"));
        entries.put(id("md_bat_wing"), sandwich("minersdelight:bat_wing", "chicken"));
        entries.put(id("md_smoked_bat_wing"), sandwich("minersdelight:smoked_bat_wing", "chicken"));
        entries.put(id("md_spider_leg"), sandwich("minersdelight:spider_leg", "spider_eye"));
        entries.put(id("md_baked_spider_leg"), sandwich("minersdelight:baked_spider_leg", "spider_eye"));
        entries.put(id("md_arthropod"), sandwich("minersdelight:arthropod", "beef"));
        entries.put(id("md_cooked_arthropod"), sandwich("minersdelight:cooked_arthropod", "beef"));
        entries.put(id("md_squid"), topping("minersdelight:squid", "squid"));
        entries.put(id("md_baked_squid"), topping("minersdelight:baked_squid", "squid"));
        entries.put(id("md_glow_squid"), topping("minersdelight:glow_squid", "squid"));
        entries.put(id("md_tentacles"), topping("minersdelight:tentacles", "squid"));
        entries.put(id("md_baked_tentacles"), topping("minersdelight:baked_tentacles", "squid"));

        entries.put(id("mnd_hoglin_loin"), topping("mynethersdelight:hoglin_loin", "pork"));
        entries.put(id("mnd_cooked_loin"), topping("mynethersdelight:cooked_loin", "pork"));
        entries.put(id("mnd_raw_sausage"), topping("mynethersdelight:raw_sausage", "sausage"));
        entries.put(id("mnd_cooked_sausage"), topping("mynethersdelight:cooked_sausage", "sausage"));
        entries.put(id("mnd_hoglin_sausage"), topping("mynethersdelight:hoglin_sausage", "sausage"));
        entries.put(id("mnd_roasted_sausage"), topping("mynethersdelight:roasted_sausage", "sausage"));
        entries.put(id("mnd_strider_slice"), topping("mynethersdelight:strider_slice", "beef"));
        entries.put(id("mnd_minced_strider"), topping("mynethersdelight:minced_strider", "beef"));
        entries.put(id("mnd_boiled_egg"), topping("mynethersdelight:boiled_egg", "fried_egg"));
        entries.put(id("mnd_roast_ear"), topping("mynethersdelight:roast_ear", "creamed_corn"));
        entries.put(id("mnd_bullet_pepper"), topping("mynethersdelight:bullet_pepper", "bullet_pepper"));
        entries.put(id("mnd_pepper_powder"), pizza("mynethersdelight:pepper_powder", "bullet_pepper"));

        iceCreamFlavours(entries);
    }

    private static void iceCreamFlavours(Map<ResourceLocation, Entry> entries) {
        flavour(entries, "minecraft:apple", "apple");
        flavour(entries, "minecraft:golden_apple", "mango");
        flavour(entries, "minecraft:enchanted_golden_apple", "mango");
        flavour(entries, "minecraft:sweet_berries", "berry");
        flavour(entries, "minecraft:glow_berries", "glow_berry");
        flavour(entries, "minecraft:melon_slice", "melon");
        flavour(entries, "minecraft:chorus_fruit", "chorus");
        flavour(entries, "minecraft:honey_bottle", "caramel");
        flavour(entries, "minecraft:cocoa_beans", "chocolate");
        flavour(entries, "minecraft:sugar", "vanilla");
        flavour(entries, "minecraft:snowball", "snow");
        flavour(entries, "modest_meals:jam", "berry");
        flavour(entries, "farmersdelight:pumpkin_slice", "pumpkin");

        flavour(entries, "culturaldelights:lemon", "lemon");
        flavour(entries, "culturaldelights:mint", "mint");
        flavour(entries, "culturaldelights:cinnamon", "cinnamon");
        flavour(entries, "culturaldelights:avocado", "avocado");
        flavour(entries, "culturaldelights:cut_avocado", "avocado");

        flavour(entries, "neapolitan:strawberries", "berry");
        flavour(entries, "neapolitan:white_strawberries", "berry");
        flavour(entries, "neapolitan:banana", "banana");
        flavour(entries, "neapolitan:dried_banana", "banana");
        flavour(entries, "neapolitan:chocolate_bar", "chocolate");
        flavour(entries, "neapolitan:vanilla_pods", "vanilla");
        flavour(entries, "neapolitan:dried_vanilla_pods", "vanilla");
        flavour(entries, "neapolitan:mint_leaves", "mint");
        flavour(entries, "neapolitan:ice_cubes", "snow");
        flavour(entries, "neapolitan:cinnamon_sticks", "cinnamon");
        flavour(entries, "neapolitan:roasted_adzuki_beans", "adzuki");
        flavour(entries, "neapolitan:mango", "mango");
        flavour(entries, "neapolitan:dried_mango", "mango");

        flavour(entries, "environmental:cherries", "cherry");
        flavour(entries, "environmental:plum", "grape");

        flavour(entries, "bountifulfares:orange", "orange");
        flavour(entries, "bountifulfares:lemon", "lemon");
        flavour(entries, "bountifulfares:plum", "grape");
        flavour(entries, "bountifulfares:passion_fruit", "mango");
        flavour(entries, "bountifulfares:elderberries", "blueberry");
        flavour(entries, "bountifulfares:lapisberries", "lapisberry");
        flavour(entries, "bountifulfares:walnut", "nut");
        flavour(entries, "bountifulfares:coconut_half", "coconut");
        flavour(entries, "bountifulfares:hoary_apple", "apple");

        flavour(entries, "brewinandchewin:red_grapes", "grape");
        flavour(entries, "brewinandchewin:white_grapes", "pear");
        flavour(entries, "brewinandchewin:sweet_berry_jam", "berry");
        flavour(entries, "brewinandchewin:glow_berry_marmalade", "glow_berry");
        flavour(entries, "brewinandchewin:apple_jelly", "apple");

        flavour(entries, "nomansland:maple_syrup_bottle", "caramel");
        flavour(entries, "nomansland:walnuts", "nut");
        flavour(entries, "nomansland:pine_nuts", "nut");
        flavour(entries, "nomansland:pear", "pear");
        flavour(entries, "nomansland:candied_pear", "pear");

        flavourTag(entries, "c:foods/chocolate_bar", "chocolate", 10);
        flavourTag(entries, "c:foods/chocolate", "chocolate", 10);
        flavourTag(entries, "c:foods/strawberry", "berry", 10);
        flavourTag(entries, "c:foods/banana", "banana", 10);
        flavourTag(entries, "c:foods/cherry", "cherry", 10);
        flavourTag(entries, "c:foods/plum", "grape", 10);
        flavourTag(entries, "c:foods/orange", "orange", 10);
        flavourTag(entries, "c:foods/elderberry", "blueberry", 10);
        flavourTag(entries, "c:foods/lapisberry", "lapisberry", 10);
        flavourTag(entries, "c:foods/passion_fruit", "mango", 10);
        flavourTag(entries, "c:foods/pear", "pear", 10);
        flavourTag(entries, "c:crops/lemon", "lemon", 10);
        flavourTag(entries, "c:fruits/lemon", "lemon", 10);
        flavourTag(entries, "c:crops/mango", "mango", 10);
        flavourTag(entries, "c:crops/ginger", "ginger", 10);
        flavourTag(entries, "c:foods/cookie", "cookie", 5);
        flavourTag(entries, "c:foods/candy", "candy", 5);
        flavourTag(entries, "c:foods/nuts", "nut", 5);
        flavourTag(entries, "c:foods/berry", "berry", 5);
        flavourTag(entries, "c:foods/fruit", "fruit", 1);
        flavourTag(entries, Constants.MOD_ID + ":jam_fruits", "fruit", 1);
        flavourTag(entries, Constants.MOD_ID + ":dessert_ingredients", "vanilla", 0);
    }

    private static void flavour(Map<ResourceLocation, Entry> entries, String item, String flavour) {
        ResourceLocation itemId = ResourceLocation.parse(item);
        ResourceLocation key = entries.entrySet().stream()
                .filter(entry -> entry.getValue().item().equals(item))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseGet(() -> id(itemId.getNamespace().equals("minecraft")
                        ? itemId.getPath()
                        : itemId.getNamespace() + "_" + itemId.getPath()));
        Entry existing = entries.get(key);
        Map<ResourceLocation, MealLayerVisual> meals = new LinkedHashMap<>();
        if (existing != null) {
            meals.putAll(existing.meals());
        }
        meals.put(ICE_CREAM_ID, iceCream(flavour));
        entries.put(key, new Entry(item, meals));
    }

    private static void flavourTag(Map<ResourceLocation, Entry> entries, String tag, String flavour, int priority) {
        String name = "ice_cream/" + tag.replace(':', '_').replace('/', '_');
        entries.put(id(name), new Entry("#" + tag, Map.of(ICE_CREAM_ID, iceCream(flavour)), priority));
    }

    public static class Builder {
        private final String item;
        private final Map<ResourceLocation, MealLayerVisual> meals = new LinkedHashMap<>();

        public Builder(String item) {
            this.item = item;
        }

        public Builder sandwich(String baseSprite) {
            return meal(SANDWICH_ID, MealLayerVisual.triplet(sandwichSprite(baseSprite)));
        }

        public Builder wrap(String baseSprite) {
            return meal(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "wrap"),
                    MealLayerVisual.triplet(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/wrap/" + baseSprite)));
        }

        public Builder pizza(String toppingSprite) {
            return meal(PIZZA_ID, pizzaToppings("pizza", toppingSprite))
                    .meal(PIZZA_SLICE_ID, pizzaToppings("pizza_slice", toppingSprite))
                    .meal(PLACED_PIZZA_ID, pizzaToppings("placed_pizza", toppingSprite));
        }

        public Builder soup(String sprite) {
            return meal(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "soup"),
                    MealLayerVisual.single(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/soup/" + sprite)));
        }

        public Builder meal(ResourceLocation mealId, MealLayerVisual visual) {
            this.meals.put(mealId, visual);
            return this;
        }

        public Entry build() {
            return new Entry(item, meals);
        }
    }

    public record Entry(
            String item,
            Map<ResourceLocation, MealLayerVisual> meals,
            int priority
    ) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("item").forGetter(Entry::item),
                Codec.unboundedMap(ResourceLocation.CODEC, MealLayerVisual.CODEC).fieldOf("meals").forGetter(Entry::meals),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(Entry::priority)
        ).apply(instance, Entry::new));

        public Entry(String item, Map<ResourceLocation, MealLayerVisual> meals) {
            this(item, meals, 0);
        }
    }
}
