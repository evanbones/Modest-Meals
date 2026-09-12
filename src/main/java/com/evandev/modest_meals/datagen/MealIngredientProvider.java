package com.evandev.modest_meals.datagen;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.ingredient.MealLayerVisual;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class MealIngredientProvider extends JsonDataProvider<MealIngredientProvider.Entry> {

    private static final ResourceLocation SANDWICH_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sandwich");
    private static final ResourceLocation WRAP_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "wrap");

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

    private static Entry sandwich(String item, String baseSprite) {
        return new Entry(item, Map.of(
                SANDWICH_ID, MealLayerVisual.triplet(sandwichSprite(baseSprite)),
                WRAP_ID, MealLayerVisual.triplet(wrapSprite(baseSprite))
        ));
    }

    public static Builder builder(String item) {
        return new Builder(item);
    }

    @Override
    protected void collect(Map<ResourceLocation, Entry> entries) {
        entries.put(id("apple"), sandwich("minecraft:apple", "apple"));
        entries.put(id("baked_potato"), sandwich("minecraft:baked_potato", "baked_potato"));
        entries.put(id("beetroot"), sandwich("minecraft:beetroot", "beetroot"));
        entries.put(id("carrot"), sandwich("minecraft:carrot", "carrot"));
        entries.put(id("chorus_fruit"), sandwich("minecraft:chorus_fruit", "chorus_fruit"));
        entries.put(id("cocoa_beans"), sandwich("minecraft:cocoa_beans", "cocoa_beans"));
        entries.put(id("glow_berries"), sandwich("minecraft:glow_berries", "glow_berries"));
        entries.put(id("honey_bottle"), sandwich("minecraft:honey_bottle", "honey"));
        entries.put(id("kelp"), sandwich("minecraft:kelp", "kelp"));
        entries.put(id("dried_kelp"), sandwich("minecraft:dried_kelp", "kelp"));
        entries.put(id("melon_slice"), sandwich("minecraft:melon_slice", "melon"));
        entries.put(id("sweet_berries"), sandwich("minecraft:sweet_berries", "sweet_berries"));
        entries.put(id("rotten_flesh"), sandwich("minecraft:rotten_flesh", "rotten_flesh"));
        entries.put(id("spider_eye"), sandwich("minecraft:spider_eye", "spider_eye"));

        entries.put(id("cooked_beef"), sandwich("minecraft:cooked_beef", "beef"));
        entries.put(id("beef"), sandwich("minecraft:beef", "beef"));
        entries.put(id("cooked_chicken"), sandwich("minecraft:cooked_chicken", "chicken"));
        entries.put(id("chicken"), sandwich("minecraft:chicken", "chicken"));
        entries.put(id("cooked_porkchop"), sandwich("minecraft:cooked_porkchop", "pork"));
        entries.put(id("porkchop"), sandwich("minecraft:porkchop", "pork"));
        entries.put(id("cooked_mutton"), sandwich("minecraft:cooked_mutton", "mutton"));
        entries.put(id("mutton"), sandwich("minecraft:mutton", "mutton"));
        entries.put(id("cooked_rabbit"), sandwich("minecraft:cooked_rabbit", "rabbit"));
        entries.put(id("rabbit"), sandwich("minecraft:rabbit", "rabbit"));
        entries.put(id("cooked_cod"), sandwich("minecraft:cooked_cod", "cod"));
        entries.put(id("cod"), sandwich("minecraft:cod", "cod"));
        entries.put(id("cooked_salmon"), sandwich("minecraft:cooked_salmon", "salmon"));
        entries.put(id("salmon"), sandwich("minecraft:salmon", "salmon"));

        entries.put(id("cabbage"), sandwich("farmersdelight:cabbage", "cabbage"));
        entries.put(id("cabbage_leaf"), sandwich("farmersdelight:cabbage_leaf", "cabbage"));
        entries.put(id("onion"), sandwich("farmersdelight:onion", "onion"));
        entries.put(id("tomato"), sandwich("farmersdelight:tomato", "tomato"));
        entries.put(id("fried_egg"), sandwich("farmersdelight:fried_egg", "fried_egg"));
        entries.put(id("cooked_rice"), sandwich("farmersdelight:cooked_rice", "cooked_rice"));
        entries.put(id("smoked_ham"), sandwich("farmersdelight:smoked_ham", "smoked_ham"));
        entries.put(id("ham"), sandwich("farmersdelight:ham", "smoked_ham"));
        entries.put(id("pumpkin_slice"), sandwich("farmersdelight:pumpkin_slice", "pumpkin"));

        entries.put(id("minced_beef"), sandwich("farmersdelight:minced_beef", "beef"));
        entries.put(id("beef_patty"), sandwich("farmersdelight:beef_patty", "beef"));
        entries.put(id("chicken_cuts"), sandwich("farmersdelight:chicken_cuts", "chicken"));
        entries.put(id("cooked_chicken_cuts"), sandwich("farmersdelight:cooked_chicken_cuts", "chicken"));
        entries.put(id("bacon"), sandwich("farmersdelight:bacon", "pork"));
        entries.put(id("cooked_bacon"), sandwich("farmersdelight:cooked_bacon", "pork"));
        entries.put(id("mutton_chops"), sandwich("farmersdelight:mutton_chops", "mutton"));
        entries.put(id("cooked_mutton_chops"), sandwich("farmersdelight:cooked_mutton_chops", "mutton"));
        entries.put(id("salmon_slice"), sandwich("farmersdelight:salmon_slice", "salmon"));
        entries.put(id("cooked_salmon_slice"), sandwich("farmersdelight:cooked_salmon_slice", "salmon"));
        entries.put(id("cod_slice"), sandwich("farmersdelight:cod_slice", "cod"));
        entries.put(id("cooked_cod_slice"), sandwich("farmersdelight:cooked_cod_slice", "cod"));

        entries.put(id("refried_beans"), sandwich("culturaldelights:refried_beans", "refried_beans"));
        entries.put(id("cinnamon"), sandwich("culturaldelights:cinnamon", "cinnamon"));
        entries.put(id("butter"), sandwich("culturaldelights:butter", "butter"));
        entries.put(id("cheese_wedge"), sandwich("culturaldelights:cheese_wedge", "cheese"));
        entries.put(id("cooked_sausage"), sandwich("culturaldelights:cooked_sausage", "sausage"));
        entries.put(id("raw_sausage"), sandwich("culturaldelights:raw_sausage", "sausage"));
        entries.put(id("popcorn"), sandwich("culturaldelights:popcorn", "popcorn"));
        entries.put(id("creamed_corn"), sandwich("culturaldelights:creamed_corn", "creamed_corn"));
        entries.put(id("tortilla_chips"), sandwich("culturaldelights:tortilla_chips", "tortilla_chips"));

        entries.put(id("avocado"), sandwich("culturaldelights:avocado", "avocado"));
        entries.put(id("cut_avocado"), sandwich("culturaldelights:cut_avocado", "avocado"));
        entries.put(id("cucumber"), sandwich("culturaldelights:cucumber", "cucumber"));
        entries.put(id("cut_cucumber"), sandwich("culturaldelights:cut_cucumber", "cucumber"));
        entries.put(id("pickle"), sandwich("culturaldelights:pickle", "pickle"));
        entries.put(id("cut_pickle"), sandwich("culturaldelights:cut_pickle", "pickle"));
        entries.put(id("eggplant"), sandwich("culturaldelights:eggplant", "eggplant"));
        entries.put(id("cut_eggplant"), sandwich("culturaldelights:cut_eggplant", "eggplant"));
        entries.put(id("smoked_eggplant"), sandwich("culturaldelights:smoked_eggplant", "eggplant"));
        entries.put(id("white_eggplant"), sandwich("culturaldelights:white_eggplant", "eggplant"));
        entries.put(id("smoked_cut_eggplant"), sandwich("culturaldelights:smoked_cut_eggplant", "eggplant"));
        entries.put(id("smoked_white_eggplant"), sandwich("culturaldelights:smoked_white_eggplant", "eggplant"));
        entries.put(id("smoked_tomato"), sandwich("culturaldelights:smoked_tomato", "tomato"));

        entries.put(id("squid"), sandwich("culturaldelights:squid", "squid"));
        entries.put(id("cooked_squid"), sandwich("culturaldelights:cooked_squid", "squid"));
        entries.put(id("glow_squid"), sandwich("culturaldelights:glow_squid", "squid"));
        entries.put(id("raw_calamari"), sandwich("culturaldelights:raw_calamari", "squid"));
        entries.put(id("cooked_calamari"), sandwich("culturaldelights:cooked_calamari", "squid"));

        entries.put(id("duck"), sandwich("environmental:duck", "chicken"));
        entries.put(id("cooked_duck"), sandwich("environmental:cooked_duck", "chicken"));
        entries.put(id("venison"), sandwich("environmental:venison", "beef"));
        entries.put(id("cooked_venison"), sandwich("environmental:cooked_venison", "beef"));
        entries.put(id("env_cherries"), sandwich("environmental:cherries", "cherry"));
        entries.put(id("env_plum"), sandwich("environmental:plum", "plum"));
        entries.put(id("env_truffle"), sandwich("environmental:truffle", "truffle"));
        entries.put(id("env_duck_egg"), sandwich("environmental:duck_egg", "fried_egg"));
        entries.put(id("env_koi"), sandwich("environmental:koi", "salmon"));

        entries.put(id("cinnamon_sticks"), sandwich("neapolitan:cinnamon_sticks", "cinnamon"));
        entries.put(id("mint_chops"), sandwich("neapolitan:mint_chops", "mutton"));
        entries.put(id("cooked_mint_chops"), sandwich("neapolitan:cooked_mint_chops", "mutton"));
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

        entries.put(id("cooked_egg"), sandwich("bountifulfares:cooked_egg", "fried_egg"));
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
        entries.put(id("bf_maize"), sandwich("bountifulfares:maize", "creamed_corn"));
        entries.put(id("bf_leek"), sandwich("bountifulfares:leek", "onion"));
        entries.put(id("bf_hoary_apple"), sandwich("bountifulfares:hoary_apple", "apple"));
        entries.put(id("bf_spongekin_slice"), sandwich("bountifulfares:spongekin_slice", "pumpkin"));
        entries.put(id("bf_pickled_spongekin"), sandwich("bountifulfares:pickled_spongekin", "pickle"));
        entries.put(id("bf_pickled_beetroot"), sandwich("bountifulfares:pickled_beetroot", "beetroot"));

        entries.put(id("bnc_cheese_wedge"), sandwich("brewinandchewin:cheese_wedge", "cheese"));
        entries.put(id("bnc_cooked_corn"), sandwich("brewinandchewin:cooked_corn", "creamed_corn"));
        entries.put(id("bnc_popped_corn"), sandwich("brewinandchewin:popped_corn", "popcorn"));
        entries.put(id("bnc_jerky"), sandwich("brewinandchewin:jerky", "beef"));
        entries.put(id("bnc_kippers"), sandwich("brewinandchewin:kippers", "cod"));
        entries.put(id("bnc_pickled_pickles"), sandwich("brewinandchewin:pickled_pickles", "pickle"));
        entries.put(id("bnc_kimchi"), sandwich("brewinandchewin:kimchi", "cabbage"));
        entries.put(id("bnc_sweet_berry_jam"), sandwich("brewinandchewin:sweet_berry_jam", "sweet_berries"));
        entries.put(id("bnc_glow_berry_marmalade"), sandwich("brewinandchewin:glow_berry_marmalade", "glow_berries"));
        entries.put(id("bnc_apple_jelly"), sandwich("brewinandchewin:apple_jelly", "apple"));
        entries.put(id("bnc_innards"), sandwich("brewinandchewin:innards", "rotten_flesh"));
        entries.put(id("bnc_aspic_cube"), sandwich("brewinandchewin:aspic_cube", "pork"));
        entries.put(id("bnc_red_grapes"), sandwich("brewinandchewin:red_grapes", "red_grapes"));
        entries.put(id("bnc_white_grapes"), sandwich("brewinandchewin:white_grapes", "white_grapes"));

        entries.put(id("nml_raw_venison"), sandwich("nomansland:raw_venison", "beef"));
        entries.put(id("nml_cooked_venison"), sandwich("nomansland:cooked_venison", "beef"));
        entries.put(id("nml_raw_horse"), sandwich("nomansland:raw_horse", "beef"));
        entries.put(id("nml_cooked_horse"), sandwich("nomansland:cooked_horse", "beef"));
        entries.put(id("nml_raw_billhook_bass"), sandwich("nomansland:raw_billhook_bass", "salmon"));
        entries.put(id("nml_cooked_billhook_bass"), sandwich("nomansland:cooked_billhook_bass", "salmon"));
        entries.put(id("nml_frog_leg"), sandwich("nomansland:frog_leg", "chicken"));
        entries.put(id("nml_cooked_frog_leg"), sandwich("nomansland:cooked_frog_leg", "chicken"));
        entries.put(id("nml_maple_syrup_bottle"), sandwich("nomansland:maple_syrup_bottle", "honey"));
        entries.put(id("nml_pine_nuts"), sandwich("nomansland:pine_nuts", "walnut"));
        entries.put(id("nml_walnuts"), sandwich("nomansland:walnuts", "walnut"));
        entries.put(id("nml_pear"), sandwich("nomansland:pear", "pear"));
        entries.put(id("nml_field_mushroom"), sandwich("nomansland:field_mushroom", "mushroom"));
        entries.put(id("nml_grilled_mushrooms"), sandwich("nomansland:grilled_mushrooms", "mushroom"));

        entries.put(id("nmd_horse_cuts"), sandwich("nomansdelight:horse_cuts", "beef"));
        entries.put(id("nmd_cooked_horse_cuts"), sandwich("nomansdelight:cooked_horse_cuts", "beef"));
        entries.put(id("nmd_venison_chop"), sandwich("nomansdelight:venison_chop", "beef"));
        entries.put(id("nmd_cooked_venison_chop"), sandwich("nomansdelight:cooked_venison_chop", "beef"));
        entries.put(id("nmd_billhook_bass_slice"), sandwich("nomansdelight:billhook_bass_slice", "salmon"));
        entries.put(id("nmd_cooked_billhook_bass_slice"), sandwich("nomansdelight:cooked_billhook_bass_slice", "salmon"));

        entries.put(id("ad_duck_fillet"), sandwich("abnormals_delight:duck_fillet", "chicken"));
        entries.put(id("ad_cooked_duck_fillet"), sandwich("abnormals_delight:cooked_duck_fillet", "chicken"));
        entries.put(id("ad_venison_shanks"), sandwich("abnormals_delight:venison_shanks", "beef"));
        entries.put(id("ad_cooked_venison_shanks"), sandwich("abnormals_delight:cooked_venison_shanks", "beef"));
        entries.put(id("ad_pike_slice"), sandwich("abnormals_delight:pike_slice", "cod"));
        entries.put(id("ad_cooked_pike_slice"), sandwich("abnormals_delight:cooked_pike_slice", "cod"));
        entries.put(id("ad_perch_slice"), sandwich("abnormals_delight:perch_slice", "salmon"));
        entries.put(id("ad_cooked_perch_slice"), sandwich("abnormals_delight:cooked_perch_slice", "salmon"));

        entries.put(id("md_vegan_patty"), sandwich("minersdelight:vegan_patty", "beef"));
        entries.put(id("md_cave_carrot"), sandwich("minersdelight:cave_carrot", "carrot"));
        entries.put(id("md_baked_cave_carrot"), sandwich("minersdelight:baked_cave_carrot", "carrot"));
        entries.put(id("md_copper_carrot"), sandwich("minersdelight:copper_carrot", "carrot"));
        entries.put(id("md_bat_wing"), sandwich("minersdelight:bat_wing", "chicken"));
        entries.put(id("md_smoked_bat_wing"), sandwich("minersdelight:smoked_bat_wing", "chicken"));
        entries.put(id("md_spider_leg"), sandwich("minersdelight:spider_leg", "spider_eye"));
        entries.put(id("md_baked_spider_leg"), sandwich("minersdelight:baked_spider_leg", "spider_eye"));
        entries.put(id("md_arthropod"), sandwich("minersdelight:arthropod", "beef"));
        entries.put(id("md_cooked_arthropod"), sandwich("minersdelight:cooked_arthropod", "beef"));
        entries.put(id("md_squid"), sandwich("minersdelight:squid", "squid"));
        entries.put(id("md_baked_squid"), sandwich("minersdelight:baked_squid", "squid"));
        entries.put(id("md_glow_squid"), sandwich("minersdelight:glow_squid", "squid"));
        entries.put(id("md_tentacles"), sandwich("minersdelight:tentacles", "squid"));
        entries.put(id("md_baked_tentacles"), sandwich("minersdelight:baked_tentacles", "squid"));

        entries.put(id("mnd_hoglin_loin"), sandwich("mynethersdelight:hoglin_loin", "pork"));
        entries.put(id("mnd_cooked_loin"), sandwich("mynethersdelight:cooked_loin", "pork"));
        entries.put(id("mnd_raw_sausage"), sandwich("mynethersdelight:raw_sausage", "sausage"));
        entries.put(id("mnd_cooked_sausage"), sandwich("mynethersdelight:cooked_sausage", "sausage"));
        entries.put(id("mnd_strider_slice"), sandwich("mynethersdelight:strider_slice", "beef"));
        entries.put(id("mnd_minced_strider"), sandwich("mynethersdelight:minced_strider", "beef"));
        entries.put(id("mnd_boiled_egg"), sandwich("mynethersdelight:boiled_egg", "fried_egg"));
        entries.put(id("mnd_roast_ear"), sandwich("mynethersdelight:roast_ear", "creamed_corn"));
        entries.put(id("mnd_bullet_pepper"), sandwich("mynethersdelight:bullet_pepper", "bullet_pepper"));

        entries.put(id("cc_lemon"), sandwich("cookscollection:lemon", "lemon"));
        entries.put(id("cc_fried_potato"), sandwich("cookscollection:fried_potato", "baked_potato"));
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
            Map<ResourceLocation, MealLayerVisual> meals
    ) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("item").forGetter(Entry::item),
                Codec.unboundedMap(ResourceLocation.CODEC, MealLayerVisual.CODEC).fieldOf("meals").forGetter(Entry::meals)
        ).apply(instance, Entry::new));
    }
}
