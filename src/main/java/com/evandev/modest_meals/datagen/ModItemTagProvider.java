package com.evandev.modest_meals.datagen;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.registry.ModItems;
import com.evandev.modest_meals.tag.ModItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {

    private static final String FARMERS_DELIGHT = "farmersdelight";
    private static final String CULTURAL_DELIGHTS = "culturaldelights";
    private static final String COOKS_COLLECTION = "cookscollection";
    private static final String ABUNDANT_DELIGHT = "abundantdelight";
    private static final String MINERS_DELIGHT = "minersdelight";
    private static final String MY_NETHERS_DELIGHT = "mynethersdelight";
    private static final String NO_MANS_LAND = "nomansland";
    private static final String NO_MANS_DELIGHT = "no_mans_delight";
    private static final String ABNORMALS_DELIGHT = "abnormals_delight";
    private static final String NEAPOLITAN = "neapolitan";
    private static final String BREWIN_AND_CHEWIN = "brewinandchewin";

    private static final TagKey<Item> TORTILLAS = itemTag("c", "foods/tortilla");
    private static final TagKey<Item> TOMATO_SAUCES = itemTag("c", "foods/tomato_sauce");
    private static final TagKey<Item> DOUGHS = itemTag("c", "foods/dough");
    private static final TagKey<Item> BREADS = itemTag("c", "foods/bread");
    private static final TagKey<Item> PASTAS = itemTag("c", "foods/pasta");
    private static final TagKey<Item> COOKED_MEATS = itemTag("c", "foods/cooked_meat");
    private static final TagKey<Item> RAW_MEATS = itemTag("c", "foods/raw_meat");
    private static final TagKey<Item> CORN = itemTag("c", "crops/corn");
    private static final TagKey<Item> LEMONS = itemTag("c", "crops/lemon");
    private static final TagKey<Item> ORANGES = itemTag("c", "crops/orange");
    private static final TagKey<Item> MANGOES = itemTag("c", "crops/mango");
    private static final TagKey<Item> PEARS = itemTag("c", "foods/pear");
    private static final TagKey<Item> FRUITS = itemTag("c", "foods/fruit");

    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                              CompletableFuture<TagLookup<net.minecraft.world.level.block.Block>> blockTags,
                              ExistingFileHelper existingFileHelper) {
        super(output, registries, blockTags, Constants.MOD_ID, existingFileHelper);
    }

    private static ResourceLocation convention(String path) {
        return ResourceLocation.fromNamespaceAndPath("c", path);
    }

    private static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private static ResourceLocation fd(String path) {
        return ResourceLocation.fromNamespaceAndPath(FARMERS_DELIGHT, path);
    }

    private static ResourceLocation cd(String path) {
        return ResourceLocation.fromNamespaceAndPath(CULTURAL_DELIGHTS, path);
    }

    private static void addOptional(TagAppender<Item> appender, String namespace, String... paths) {
        for (String path : paths) {
            appender.addOptional(ResourceLocation.fromNamespaceAndPath(namespace, path));
        }
    }

    private static void addOptionalTags(TagAppender<Item> appender, String namespace, String... paths) {
        for (String path : paths) {
            appender.addOptionalTag(ResourceLocation.fromNamespaceAndPath(namespace, path));
        }
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        notAnIngredient();
        seedConventionFoods();
    }

    private void notAnIngredient() {
        var notAnIngredient = tag(ModItemTags.NOT_AN_INGREDIENT);

        ModItems.CREATIVE_TAB_ITEMS.forEach(item -> notAnIngredient.add(item.get()));

        notAnIngredient.add(
                Items.MUSHROOM_STEW,
                Items.RABBIT_STEW,
                Items.BEETROOT_SOUP,
                Items.SUSPICIOUS_STEW,
                Items.PUMPKIN_PIE,
                Items.CAKE,
                Items.COOKIE);

        notAnIngredient.addOptionalTag(convention("foods/soup"));
        notAnIngredient.addOptionalTag(convention("foods/pie"));
        notAnIngredient.addOptionalTag(convention("foods/cookie"));
        notAnIngredient.addOptionalTag(convention("foods/candy"));
        notAnIngredient.addOptionalTag(convention("foods/edible_when_placed"));

        notAnIngredient.addOptionalTag(fd("meals"));
        notAnIngredient.addOptionalTag(fd("pies"));
        notAnIngredient.addOptionalTag(fd("sweets"));
        notAnIngredient.addOptionalTag(fd("snacks"));
        notAnIngredient.addOptionalTag(fd("feasts"));

        addOptional(notAnIngredient, FARMERS_DELIGHT,
                "nether_salad",
                "dog_food");

        addOptional(notAnIngredient, CULTURAL_DELIGHTS,
                "elote",
                "empanada",
                "hearty_salad",
                "beef_burrito",
                "mutton_sandwich",
                "eggplant_burger",
                "avocado_toast",
                "creamed_corn",
                "chicken_taco",
                "spicy_curry",
                "pork_wrap",
                "fish_taco",
                "poached_eggplants",
                "eggplant_parmesan",
                "eggplant_parmesan_block",
                "exotic_roll_medley",
                "chicken_roll",
                "chicken_roll_slice",
                "midori_roll",
                "midori_roll_slice",
                "pufferfish_roll",
                "tropical_roll",
                "calamari_roll",
                "egg_roll",
                "rice_ball");

        notAnIngredient.addOptionalTag(convention("foods/ice_cream"));
        notAnIngredient.addOptionalTag(convention("foods/cake"));
        notAnIngredient.addOptionalTag(convention("drinks/milkshake"));

        addOptionalTags(notAnIngredient, BREWIN_AND_CHEWIN,
                "fermented_drinks",
                "wines",
                "distillates");

        addOptional(notAnIngredient, COOKS_COLLECTION,
                "lemonade",
                "chocolate_muffin",
                "lemon_muffin",
                "fish_and_chips",
                "rustic_loaf");

        addOptional(notAnIngredient, ABUNDANT_DELIGHT,
                "rabbit_sandwich",
                "poreshroom_on_log",
                "cooked_poreshroom_on_log",
                "wisp_cheesecake",
                "wisp_cheesecake_slice");

        addOptional(notAnIngredient, MINERS_DELIGHT,
                "bat_rolls",
                "cave_hamburger",
                "improvised_barbecue_stick",
                "insect_sandwich",
                "insect_wrap",
                "squid_sandwich",
                "tentacles_on_a_stick",
                "takoyaki",
                "weird_caviar",
                "crunchy_bar",
                "nutritional_bar",
                "golden_nutritional_bar",
                "vegan_hamburger",
                "vegan_wrap",
                "fake_meatloaf",
                "stuffed_squid",
                "glazed_arachnid_limbs",
                "beetroot_soup_cup",
                "mushroom_stew_cup",
                "rabbit_stew_cup",
                "baked_cod_stew_cup",
                "noodle_soup_cup",
                "beef_stew_cup",
                "chicken_soup_cup",
                "fish_stew_cup",
                "pumpkin_soup_cup",
                "vegetable_soup_cup",
                "bone_broth_cup",
                "onion_soup_cup",
                "cave_soup_cup",
                "bat_soup_cup",
                "insect_stew_cup");

        addOptional(notAnIngredient, MY_NETHERS_DELIGHT,
                "deviled_egg",
                "scotch_eggs",
                "bleeding_tartar",
                "strider_with_grilled_fungus",
                "strider_stew",
                "crimson_stroganoff",
                "plate_of_striderloaf",
                "plate_of_cold_striderloaf",
                "hotdog",
                "hotdog_with_mixed_salad",
                "hotdog_with_nether_salad",
                "sausage_and_potatoes",
                "breakfast_sampler",
                "nether_burger",
                "blue_tenderloin_steak",
                "red_loin_on_a_stick",
                "fried_hoglin_chop",
                "twisted_ghasta",
                "giant_takoyaki",
                "fries_ghasta",
                "ghast_salad",
                "dried_ghast_with_milk",
                "sizzling_pudding",
                "tear_popsicle",
                "spicy_cotton",
                "stuffed_pepper",
                "spicy_skewer",
                "chilidog",
                "spicy_hoglin_stew",
                "hot_wings",
                "hot_wings_bucket",
                "spicy_curry",
                "burnt_roll",
                "magma_cake_slice",
                "hot_cream",
                "hot_cream_cone",
                "raw_stuffed_hoglin",
                "plate_of_stuffed_hoglin",
                "plate_of_stuffed_hoglin_ham",
                "plate_of_stuffed_hoglin_snout");

        addOptional(notAnIngredient, NO_MANS_LAND,
                "pancake",
                "honeyed_apple",
                "syruped_pear",
                "trail_mix",
                "hardtack",
                "awkward_residue",
                "stallion_strip",
                "stallion_strips");

        addOptional(notAnIngredient, NO_MANS_DELIGHT,
                "horse_soup",
                "horse_soup_pot",
                "horse_wrap",
                "mushrooms_with_tomato",
                "sweet_frog_legs",
                "venison_tart",
                "shroomburger",
                "plated_venison_roulade",
                "venison_roulade_block",
                "stuffed_shelf_mushroom",
                "stuffed_shelf_mushroom_block",
                "billhook_bass_roll",
                "plated_pickled_brains",
                "pickled_brains_block",
                "boiled_cave_weeds",
                "river_meal",
                "choco_glazed_orange",
                "chocolate_cake",
                "chocolate_cake_slice",
                "chocolate_tart",
                "kozinak");

        addOptional(notAnIngredient, ABNORMALS_DELIGHT,
                "escargot",
                "maple_glazed_bacon",
                "slabdish",
                "cherry_cream_soda",
                "passion_aloe_nectar",
                "pickerelweed_juice",
                "vanilla_cake_slice",
                "chocolate_cake_slice",
                "strawberry_cake_slice",
                "banana_cake_slice",
                "mint_cake_slice",
                "adzuki_cake_slice",
                "yucca_gateau_slice");

        addOptional(notAnIngredient, NEAPOLITAN,
                "strawberry_scones",
                "banana_bread",
                "adzuki_bun",
                "adzuki_curry",
                "vanilla_pudding",
                "vanilla_fudge",
                "vanilla_chocolate_fingers",
                "chocolate_strawberries",
                "mint_chocolate",
                "strawberry_bean_bonbons",
                "strawberry_banana_smoothie",
                "cinnamon_bagel",
                "cinnamon_roll",
                "berry_strudel",
                "bubblegum",
                "strawberry_bubblegum",
                "banana_bubblegum",
                "mint_bubblegum",
                "mango_bubblegum");

        addOptional(notAnIngredient, BREWIN_AND_CHEWIN,
                "pizza",
                "pizza_slice",
                "quiche",
                "quiche_slice",
                "vegetable_omelet",
                "cheesy_pasta",
                "scarlet_pierogi",
                "horror_lasagna",
                "fiery_fondue_pot",
                "croissant",
                "raw_croissant",
                "raw_muffin",
                "corn_muffin",
                "jam_sandwich",
                "cocoa_fudge",
                "maple_fudge",
                "glow_brownie",
                "apple_turnover",
                "rice_pudding",
                "rich_chocolate_cake",
                "pumpkin_roll",
                "glow_berry_meringue_pie");
    }

    private void seedConventionFoods() {
        var tortillas = tag(TORTILLAS);
        tortillas.addOptional(cd("tortilla"));
        addOptional(tortillas, BREWIN_AND_CHEWIN, "tortilla");

        tag(TOMATO_SAUCES).addOptional(fd("tomato_sauce"));

        var doughs = tag(DOUGHS);
        doughs.addOptional(cd("corn_dough"));
        addOptional(doughs, MY_NETHERS_DELIGHT, "ghast_sourdough");

        var breads = tag(BREADS);
        addOptional(breads, COOKS_COLLECTION, "rustic_loaf_slice");
        addOptional(breads, NO_MANS_LAND, "hardtack");

        addOptional(tag(PASTAS), MY_NETHERS_DELIGHT, "ghasta");

        var cookedMeats = tag(COOKED_MEATS);
        addOptionalTags(cookedMeats, "c",
                "foods/cooked_venison",
                "foods/cooked_horse",
                "foods/cooked_duck",
                "foods/cooked_insect_meat");
        addOptional(cookedMeats, ABNORMALS_DELIGHT, "cooked_duck_fillet", "cooked_venison_shanks");

        var rawMeats = tag(RAW_MEATS);
        addOptionalTags(rawMeats, "c", "foods/raw_venison", "foods/raw_horse", "foods/raw_duck");
        addOptional(rawMeats, ABNORMALS_DELIGHT, "duck_fillet", "venison_shanks");

        addOptional(tag(CORN), BREWIN_AND_CHEWIN, "corn");
        addOptional(tag(LEMONS), COOKS_COLLECTION, "lemon");
        addOptional(tag(ORANGES), NO_MANS_DELIGHT, "orange");
        addOptional(tag(MANGOES), NEAPOLITAN, "mango");
        addOptional(tag(PEARS), NO_MANS_LAND, "pear");

        var fruits = tag(FRUITS);
        addOptional(fruits, COOKS_COLLECTION, "lemon");
        addOptional(fruits, NO_MANS_DELIGHT, "orange");
        addOptional(fruits, NEAPOLITAN, "mango", "dried_mango");
    }
}
