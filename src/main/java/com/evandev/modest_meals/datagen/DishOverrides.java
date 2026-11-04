package com.evandev.modest_meals.datagen;

import com.evandev.modest_meals.food.meal.MealType;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DishOverrides {

    private static final String FD = "farmersdelight:";
    private static final String CD = "culturaldelights:";
    private static final String BF = "bountifulfares:";
    private static final String MND = "mynethersdelight:";
    private static final String NMD = "nomansdelight:";
    private static final String NML = "nomansland:";
    private static final String NEA = "neapolitan:";
    private static final String AD = "abnormals_delight:";
    private static final String UA = "upgrade_aquatic:";
    private static final String BNC = "brewinandchewin:";

    private static final String RAW_BEEF = "#c:foods/raw_beef";
    private static final String RAW_CHICKEN = "#c:foods/raw_chicken";
    private static final String COOKED_CHICKEN = "#c:foods/cooked_chicken";
    private static final String COOKED_MUTTON = "#c:foods/cooked_mutton";
    private static final String COOKED_EGG = "#c:foods/cooked_egg";
    private static final String COOKED_FISH = "#c:foods/cooked_fish";
    private static final String CARROT = "#c:crops/carrot";
    private static final String POTATO = "#c:crops/potato";
    private static final String BEETROOT = "#c:crops/beetroot";
    private static final String TOMATO = "#c:crops/tomato";
    private static final String ONION = "#c:crops/onion";
    private static final String CORN = "#c:crops/corn";
    private static final String GREENS = "#c:foods/leafy_green";
    private static final String VEGETABLE = "#c:foods/vegetable";
    private static final String TOMATO_SAUCE = FD + "tomato_sauce";
    private static final String TOFU = CD + "tofu";
    private static final String REFRIED_BEANS = CD + "refried_beans";
    private static final String AVOCADO = "#culturaldelights:avocados";
    private static final String STRAWBERRY = "#c:foods/strawberry";
    private static final String BANANA = "#c:foods/banana";
    private static final String CHOCOLATE = "#c:foods/chocolate_bar";
    private static final String VANILLA = NEA + "dried_vanilla_pods";
    private static final String SNOWBALL = "minecraft:snowball";
    private static final String MINT = NEA + "mint_leaves|" + CD + "mint";

    private final Map<String, List<MealType.ItemOverride>> byType = new LinkedHashMap<>();

    private DishOverrides() {
    }

    public static Map<String, List<MealType.ItemOverride>> build() {
        DishOverrides table = new DishOverrides();
        table.soups();
        table.ramen();
        table.pasta();
        table.salads();
        table.sandwiches();
        table.wraps();
        table.roll();
        table.pizza();
        table.curries();
        table.jams();
        table.pies();
        table.cakes();
        table.milkshakes();
        table.iceCream();
        return table.byType;
    }

    private void soups() {
        add("soup", FD + "beef_stew", RAW_BEEF, CARROT, POTATO);
        add("soup", FD + "chicken_soup", RAW_CHICKEN, CARROT, GREENS, VEGETABLE);
        add("soup", FD + "vegetable_soup", CARROT, POTATO, BEETROOT, GREENS);
        add("soup", FD + "fish_stew", COOKED_FISH, "minecraft:dried_kelp", "minecraft:dried_kelp");
        add("soup", FD + "fish_stew", "#c:foods/safe_raw_fish", TOMATO_SAUCE, ONION);
        add("soup", FD + "pumpkin_soup", FD + "pumpkin_slice", GREENS, "#c:foods/raw_pork|" + TOFU);
        add("soup", FD + "baked_cod_stew", "#c:foods/raw_cod", POTATO, "#c:eggs", TOMATO);
        add("soup", FD + "onion_soup", ONION, ONION, "#c:foods/bread");
        add("soup", BF + "bountiful_stew", "minecraft:cooked_porkchop", CARROT, "#c:foods/corn|" + BF + "maize");
        add("soup", BF + "leek_stew", BF + "leek", BF + "leek", BF + "leek");
        add("soup", BF + "apple_stew", "minecraft:apple", "#c:foods/elderberries", "#c:foods/elderberries");
        add("soup", BF + "coconut_stew", BF + "leek", "#c:coconut_halves", "#c:coconut_halves");
        add("soup", MND + "egg_soup", "#c:foods/boiled_egg", COOKED_EGG, COOKED_EGG, "#c:foods/onion");
        add("soup", MND + "spicy_hoglin_stew",
                MND + "hoglin_loin|" + MND + "hoglin_sausage|" + MND + "roasted_sausage|" + MND + "cooked_loin",
                POTATO, CARROT, MND + "bullet_pepper|" + MND + "pepper_powder");
        add("soup", "abundantdelight:puffball_soup", "#c:foods/puffball_mushrooms", ONION, "minecraft:egg");
        add("soup", NEA + "adzuki_stew", NEA + "adzuki_beans", NEA + "adzuki_beans", BEETROOT, TOMATO,
                "minecraft:brown_mushroom");
        add("soup", "autumnity:foul_soup", "autumnity:foul_berries", "autumnity:foul_berries",
                "minecraft:spider_eye", ONION);
        add("soup", "infernalexp:blindsight_tongue_stew", "infernalexp:blindsight_tongue",
                "minecraft:crimson_fungus", "minecraft:warped_fungus");
    }

    private void ramen() {
        add("ramen", FD + "noodle_soup", "#c:eggs|" + COOKED_EGG, "minecraft:dried_kelp", "#c:foods/raw_pork|" + TOFU);
        add("ramen", MND + "spicy_noodle_soup", COOKED_EGG, "#mynethersdelight:hot_spice", "#c:foods/raw_hoglin");
        add("ramen", MND + "crimson_stroganoff", MND + "minced_strider", "minecraft:crimson_fungus",
                "minecraft:crimson_fungus");
    }

    private void pasta() {
        add("pasta", FD + "pasta_with_meatballs", FD + "minced_beef|" + TOFU, TOMATO_SAUCE);
        add("pasta", FD + "pasta_with_mutton_chop", "#c:foods/raw_mutton|" + COOKED_MUTTON, TOMATO_SAUCE);
        add("pasta", FD + "squid_ink_pasta", "#c:foods/safe_raw_fish", TOMATO, "minecraft:ink_sac");
        add("pasta", FD + "vegetable_noodles", CARROT, "#c:mushrooms", GREENS, VEGETABLE);
        add("pasta", AD + "duck_noodles", "#c:foods/raw_duck", CARROT, VEGETABLE);
        add("pasta", NML + "pasta_with_pesto", NML + "pesto_bottle", "#nomansland:edible_mushrooms");
    }

    private void salads() {
        add("salad", FD + "mixed_salad", TOMATO, GREENS + "|" + BEETROOT);
        add("salad", BF + "sea_salad", "minecraft:kelp", "minecraft:kelp", BF + "citrus_essence");
        add("salad", MND + "ghast_salad", "#c:foods/raw_ghast", CARROT);
    }

    private void sandwiches() {
        add("sandwich", FD + "egg_sandwich", COOKED_EGG, COOKED_EGG, COOKED_EGG);
        add("sandwich", FD + "chicken_sandwich", COOKED_CHICKEN, GREENS, CARROT);
        add("sandwich", FD + "hamburger", FD + "beef_patty|" + TOFU, GREENS, TOMATO);
        add("sandwich", FD + "bacon_sandwich", "#c:foods/cooked_bacon", GREENS, TOMATO);
        add("sandwich", CD + "mutton_sandwich", COOKED_MUTTON, COOKED_EGG, BEETROOT);
        add("sandwich", CD + "eggplant_burger", "#culturaldelights:smoked_all_eggplants", GREENS, TOMATO);
        add("sandwich", "abundantdelight:rabbit_sandwich", "minecraft:cooked_rabbit", GREENS, TOMATO);
        add("sandwich", NMD + "frog_sandwich", NML + "cooked_frog_leg", NML + "cooked_frog_leg", ONION);
        add("sandwich", NMD + "shroomburger", NML + "grilled_mushrooms", "#c:foods/cooked_venison", NML + "walnuts");
    }

    private void wraps() {
        add("wrap", FD + "mutton_wrap", COOKED_MUTTON, GREENS, REFRIED_BEANS);
        add("wrap", CD + "pork_wrap", "#c:foods/cooked_pork", "minecraft:apple", GREENS);
        add("wrap", CD + "cheesy_chip_wrap", "#culturaldelights:cheese_wedges", CD + "tortilla_chips", CD + "tortilla_chips");
        add("wrap", CD + "beef_burrito", "#c:foods/cooked_beef", AVOCADO, REFRIED_BEANS);
        add("wrap", CD + "chicken_taco", COOKED_CHICKEN, REFRIED_BEANS, CORN);
        add("wrap", CD + "fish_taco", COOKED_FISH, TOMATO, GREENS);
        add("wrap", NMD + "horse_wrap", "#c:foods/cooked_horse", GREENS, TOMATO);
    }

    private void roll() {
        add("roll", FD + "salmon_roll", 2, FD + "salmon_slice", FD + "salmon_slice");
        add("roll", FD + "cod_roll", 2, FD + "cod_slice", FD + "cod_slice");
        add("roll", FD + "kelp_roll", VEGETABLE, "minecraft:dried_kelp", "minecraft:dried_kelp");
        add("roll", CD + "egg_roll", 4, COOKED_EGG, COOKED_EGG, COOKED_EGG);
        add("roll", CD + "pufferfish_roll", 2, "minecraft:pufferfish", "minecraft:pufferfish");
        add("roll", CD + "tropical_roll", 2, "minecraft:tropical_fish", "minecraft:tropical_fish");
        add("roll", CD + "calamari_roll", 2, CD + "raw_calamari", CD + "raw_calamari");
        add("roll", NMD + "billhook_bass_roll", 2, NMD + "billhook_bass_slice", NMD + "billhook_bass_slice");
    }

    private void pizza() {
        addOnBase("pizza", NMD + "pesto_pizza", List.of(NML + "pesto_bottle"),
                "#c:foods/cooked_venison", "#c:foods/cooked_venison");
    }

    private void curries() {
        add("curry", CD + "cinnamon_mint_curry", CD + "cinnamon", CD + "mint", CD + "corn_cob", RAW_CHICKEN + "|" + TOFU);
        add("curry", CD + "spicy_curry", "#mynethersdelight:hot_spice|#c:crops/ginger", TOMATO_SAUCE, CD + "beans", RAW_CHICKEN + "|" + TOFU);
        add("curry", NEA + "adzuki_curry", NEA + "adzuki_beans|" + NEA + "roasted_adzuki_beans",
                BANANA + "|" + NEA + "dried_banana", CARROT, FD + "pumpkin_slice");
    }

    private void iceCream() {
        add("ice_cream_bowl", NEA + "neapolitan_ice_cream", CHOCOLATE, VANILLA, STRAWBERRY);
        flavouredIceCream("vanilla", VANILLA);
        flavouredIceCream("chocolate", CHOCOLATE);
        flavouredIceCream("strawberry", STRAWBERRY);
        flavouredIceCream("banana", BANANA);
        flavouredIceCream("mint", MINT);
        flavouredIceCream("adzuki", NEA + "roasted_adzuki_beans");
    }

    private void flavouredIceCream(String flavour, String ingredient) {
        add("ice_cream_bowl", NEA + flavour + "_ice_cream", ingredient);
        add("ice_cream_bowl", NEA + flavour + "_ice_cream", ingredient, ingredient, ingredient);
    }

    private void jams() {
        add("jam", "abundant_atmosphere:squashberry_jam", 2, "abundant_atmosphere:roasted_gourdnut",
                "#abundant_atmosphere:squashberry_jam_fruits", "minecraft:sweet_berries");
        add("jam", UA + "mulberry_jam_bottle", UA + "mulberry");
    }

    private void pies() {
        add("pie", FD + "apple_pie", "minecraft:apple", "minecraft:apple", "minecraft:apple");
        add("pie", FD + "chocolate_pie", "minecraft:cocoa_beans", "minecraft:cocoa_beans", "minecraft:cocoa_beans");
        add("pie", FD + "sweet_berry_cheesecake", "minecraft:sweet_berries", "minecraft:sweet_berries",
                "minecraft:sweet_berries");
        addAlongside("pie", "minecraft:pumpkin_pie", FD + "pumpkin_slice", FD + "pumpkin_slice", FD + "pumpkin_slice");
        add("pie", BNC + "glow_berry_meringue_pie", "minecraft:glow_berries", "minecraft:glow_berries",
                "minecraft:glow_berries");
        add("pie", BNC + "quiche", "#c:foods/cooked_bacon|minecraft:brown_mushroom",
                "#c:foods/cooked_bacon|minecraft:brown_mushroom", "#brewinandchewin:foods/cheese_wedge");
        add("pie", NML + "pear_cobbler", NML + "pear", NML + "walnuts");
        add("pie", BF + "apple_pie", "minecraft:apple", "minecraft:apple");
        add("pie", BF + "orange_pie", "#c:foods/oranges", "#c:foods/oranges", "#c:foods/oranges");
        add("pie", BF + "lemon_pie", "#c:foods/lemons", "#c:foods/lemons", "#c:foods/lemons");
        add("pie", BF + "plum_pie", "#c:foods/plums", "#c:foods/plums", "#c:foods/plums");
        add("pie", BF + "hoary_pie", BF + "hoary_apple", BF + "hoary_apple", BF + "hoary_apple");
        add("pie", BF + "melon_pie", "minecraft:melon_slice", "minecraft:melon_slice", "minecraft:melon_slice");
        add("pie", BF + "elderberry_tart", "#c:foods/elderberries", "#c:foods/elderberries");
        add("pie", BF + "glow_berry_tart", "minecraft:glow_berries", "minecraft:glow_berries");
        add("pie", BF + "lapisberry_tart", BF + "lapisberries", BF + "lapisberries");
        add("pie", BF + "passion_fruit_tart", "#c:foods/passion_fruit", "#c:foods/passion_fruit");
        add("pie", BF + "sweet_berry_tart", "minecraft:sweet_berries", "minecraft:sweet_berries");
    }

    private void cakes() {
        add("cake", NEA + "vanilla_cake", VANILLA, VANILLA);
        add("cake", NEA + "chocolate_cake", CHOCOLATE, CHOCOLATE);
        add("cake", NEA + "strawberry_cake", STRAWBERRY, STRAWBERRY);
        add("cake", NEA + "banana_cake", BANANA, BANANA);
        add("cake", NEA + "mint_cake", MINT, MINT);
        add("cake", NEA + "adzuki_cake", NEA + "roasted_adzuki_beans", NEA + "roasted_adzuki_beans");
        add("cake", BF + "cocoa_cake", "minecraft:cocoa_beans", "minecraft:cocoa_beans", "minecraft:cocoa_beans");
        add("cake", BF + "coconut_cake", "#c:coconut_halves", "#c:coconut_halves", "#c:coconut_halves");
        add("cake", NML + "fruit_cake", NML + "pear", "minecraft:sweet_berries", "minecraft:apple");
        add("cake", BNC + "rich_chocolate_cake", BNC + "cocoa_fudge", BNC + "cocoa_fudge", "minecraft:sweet_berries");
        add("cake", BNC + "pumpkin_roll", FD + "pumpkin_slice", FD + "pumpkin_slice", FD + "pumpkin_slice");
        add("cake", MND + "magma_cake_block", "minecraft:magma_cream", "minecraft:magma_cream", MND + "hot_cream");
    }

    private void milkshakes() {
        addAlongside("milkshake", NEA + "vanilla_milkshake", VANILLA);
        addAlongside("milkshake", NEA + "chocolate_milkshake", CHOCOLATE);
        addAlongside("milkshake", NEA + "strawberry_milkshake", STRAWBERRY);
        addAlongside("milkshake", NEA + "banana_milkshake", BANANA);
        addAlongside("milkshake", NEA + "mint_milkshake", MINT);
        addAlongside("milkshake", NEA + "adzuki_milkshake", NEA + "roasted_adzuki_beans");
    }

    private void addAlongside(String type, String result, String... ingredients) {
        byType.computeIfAbsent(type, key -> new ArrayList<>())
                .add(new MealType.ItemOverride(ResourceLocation.parse(result), 1, List.of(ingredients), false));
    }

    private void add(String type, String result, String... ingredients) {
        add(type, result, 1, ingredients);
    }

    private void add(String type, String result, int count, String... ingredients) {
        byType.computeIfAbsent(type, key -> new ArrayList<>())
                .add(new MealType.ItemOverride(ResourceLocation.parse(result), count, List.of(ingredients), true));
    }

    private void addOnBase(String type, String result, List<String> base, String... ingredients) {
        byType.computeIfAbsent(type, key -> new ArrayList<>())
                .add(new MealType.ItemOverride(ResourceLocation.parse(result), 1, List.of(ingredients), true, base));
    }
}
