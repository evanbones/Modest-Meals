package com.evandev.modest_meals.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;

import java.util.LinkedHashMap;

public class DefaultFoodStackSizes {

    public static LinkedHashMap<String, Integer> getDefaultVanillaStackSizes() {
        var sizes = new LinkedHashMap<Item, Integer>();
        // nutrition = 1. Total 64
        sizes.put(Items.BEETROOT, 64);
        sizes.put(Items.DRIED_KELP, 64);

        // nutrition = 2. Total 64
        sizes.put(Items.COOKIE, 32);
        sizes.put(Items.GLOW_BERRIES, 32);
        sizes.put(Items.MELON_SLICE, 32);
        sizes.put(Items.SWEET_BERRIES, 32);

        // 3 <= nutrition <= 4. Total 48 - 64
        sizes.put(Items.APPLE, 16);
        sizes.put(Items.ENCHANTED_GOLDEN_APPLE, 16);
        sizes.put(Items.GOLDEN_APPLE, 16);
        sizes.put(Items.POTATO, 16);
        sizes.put(Items.BAKED_POTATO, 16);
        sizes.put(Items.POISONOUS_POTATO, 16);
        sizes.put(Items.CARROT, 16);
        sizes.put(Items.GOLDEN_CARROT, 16);
        sizes.put(Items.CHORUS_FRUIT, 16);

        // 5 <= nutrition <= 6. Total 40 - 48
        sizes.put(Items.BREAD, 8);
        sizes.put(Items.BEETROOT_SOUP, 8);
        sizes.put(Items.COD, 8);
        sizes.put(Items.COOKED_COD, 8);
        sizes.put(Items.SALMON, 8);
        sizes.put(Items.COOKED_SALMON, 8);
        sizes.put(Items.PUFFERFISH, 8);
        sizes.put(Items.TROPICAL_FISH, 8);
        sizes.put(Items.CHICKEN, 8);
        sizes.put(Items.COOKED_CHICKEN, 8);
        sizes.put(Items.MUTTON, 8);
        sizes.put(Items.COOKED_MUTTON, 8);
        sizes.put(Items.RABBIT, 8);
        sizes.put(Items.COOKED_RABBIT, 8);
        sizes.put(Items.HONEY_BOTTLE, 8);
        sizes.put(Items.MUSHROOM_STEW, 8);

        // nutrition = 8. Total 36
        sizes.put(Items.BEEF, 6);
        sizes.put(Items.COOKED_BEEF, 6);
        sizes.put(Items.PORKCHOP, 6);
        sizes.put(Items.COOKED_PORKCHOP, 6);
        sizes.put(Items.PUMPKIN_PIE, 6);

        // nutrition = 10. Total 40
        sizes.put(Items.RABBIT_STEW, 4);

        // unstackable
        sizes.put(Items.SUSPICIOUS_STEW, 1);

        var result = new LinkedHashMap<String, Integer>();
        for (var entry : sizes.entrySet()) {
            result.put(BuiltInRegistries.ITEM.getKey(entry.getKey()).toString(), entry.getValue());
        }
        return result;
    }

    public static LinkedHashMap<String, Integer> getDefaultFarmersDelightStackSizes() {
        var result = new LinkedHashMap<String, Integer>();
        // Check if Farmers Delight is loaded
        if (!ModList.get().isLoaded("farmersdelight")) {
            return result;
        }

        // We use string IDs to remain robust even without hard dependency at runtime
        addFdEntry(result, "farmersdelight:cabbage_leaf", 64);
        addFdEntry(result, "farmersdelight:tomato", 64);

        addFdEntry(result, "farmersdelight:cabbage", 32);
        addFdEntry(result, "farmersdelight:honey_cookie", 32);
        addFdEntry(result, "farmersdelight:onion", 32);
        addFdEntry(result, "farmersdelight:pie_crust", 32);
        addFdEntry(result, "farmersdelight:pumpkin_slice", 32);
        addFdEntry(result, "farmersdelight:raw_pasta", 32);
        addFdEntry(result, "farmersdelight:sweet_berry_cookie", 32);
        addFdEntry(result, "farmersdelight:wheat_dough", 32);

        addFdEntry(result, "farmersdelight:apple_pie_slice", 24);
        addFdEntry(result, "farmersdelight:cake_slice", 24);
        addFdEntry(result, "farmersdelight:chocolate_pie_slice", 24);
        addFdEntry(result, "farmersdelight:sweet_berry_cheesecake_slice", 24);
        addFdEntry(result, "farmersdelight:melon_popsicle", 24);
        addFdEntry(result, "farmersdelight:chicken_cuts", 24);
        addFdEntry(result, "farmersdelight:cooked_chicken_cuts", 24);
        addFdEntry(result, "farmersdelight:cod_slice", 24);
        addFdEntry(result, "farmersdelight:cooked_cod_slice", 24);
        addFdEntry(result, "farmersdelight:mutton_chops", 24);
        addFdEntry(result, "farmersdelight:cooked_mutton_chops", 24);
        addFdEntry(result, "farmersdelight:salmon_slice", 24);
        addFdEntry(result, "farmersdelight:cooked_salmon_slice", 24);

        addFdEntry(result, "farmersdelight:minced_beef", 16);
        addFdEntry(result, "farmersdelight:beef_patty", 16);
        addFdEntry(result, "farmersdelight:bacon", 16);
        addFdEntry(result, "farmersdelight:cooked_bacon", 16);
        addFdEntry(result, "farmersdelight:dog_food", 16);
        addFdEntry(result, "farmersdelight:fried_egg", 16);
        addFdEntry(result, "farmersdelight:tomato_sauce", 16);
        addFdEntry(result, "farmersdelight:cabbage_rolls", 16);
        addFdEntry(result, "farmersdelight:nether_salad", 16);

        addFdEntry(result, "farmersdelight:cooked_rice", 12);
        addFdEntry(result, "farmersdelight:fruit_salad", 12);
        addFdEntry(result, "farmersdelight:kelp_roll_slice", 12);
        addFdEntry(result, "farmersdelight:mixed_salad", 12);
        addFdEntry(result, "farmersdelight:cod_roll", 12);
        addFdEntry(result, "farmersdelight:glow_berry_custard", 12);
        addFdEntry(result, "farmersdelight:salmon_roll", 12);

        addFdEntry(result, "farmersdelight:barbecue_stick", 8);
        addFdEntry(result, "farmersdelight:bone_broth", 8);
        addFdEntry(result, "farmersdelight:dumplings", 8);
        addFdEntry(result, "farmersdelight:egg_sandwich", 8);
        addFdEntry(result, "farmersdelight:bacon_and_eggs", 8);
        addFdEntry(result, "farmersdelight:bacon_sandwich", 8);
        addFdEntry(result, "farmersdelight:chicken_sandwich", 8);
        addFdEntry(result, "farmersdelight:mutton_wrap", 8);
        addFdEntry(result, "farmersdelight:ratatouille", 8);
        addFdEntry(result, "farmersdelight:ham", 8);
        addFdEntry(result, "farmersdelight:smoked_ham", 8);
        addFdEntry(result, "farmersdelight:stuffed_potato", 8);

        addFdEntry(result, "farmersdelight:apple_pie", 6);
        addFdEntry(result, "farmersdelight:beef_stew", 6);
        addFdEntry(result, "farmersdelight:chocolate_pie", 6);
        addFdEntry(result, "farmersdelight:fish_stew", 6);
        addFdEntry(result, "farmersdelight:hamburger", 6);
        addFdEntry(result, "farmersdelight:kelp_roll", 6);
        addFdEntry(result, "farmersdelight:mushroom_rice", 6);
        addFdEntry(result, "farmersdelight:pasta_with_meatballs", 6);
        addFdEntry(result, "farmersdelight:pasta_with_mutton_chop", 6);
        addFdEntry(result, "farmersdelight:steak_and_potatoes", 6);
        addFdEntry(result, "farmersdelight:sweet_berry_cheesecake", 6);
        addFdEntry(result, "farmersdelight:vegetable_soup", 6);

        addFdEntry(result, "farmersdelight:baked_cod_stew", 4);
        addFdEntry(result, "farmersdelight:chicken_soup", 4);
        addFdEntry(result, "farmersdelight:fried_rice", 4);
        addFdEntry(result, "farmersdelight:grilled_salmon", 4);
        addFdEntry(result, "farmersdelight:honey_glazed_ham", 4);
        addFdEntry(result, "farmersdelight:noodle_soup", 4);
        addFdEntry(result, "farmersdelight:pumpkin_soup", 4);
        addFdEntry(result, "farmersdelight:roasted_mutton_chops", 4);
        addFdEntry(result, "farmersdelight:roast_chicken", 4);
        addFdEntry(result, "farmersdelight:shepherds_pie", 4);
        addFdEntry(result, "farmersdelight:squid_ink_pasta", 4);
        addFdEntry(result, "farmersdelight:stuffed_pumpkin", 4);
        addFdEntry(result, "farmersdelight:vegetable_noodles", 4);

        return result;
    }

    private static void addFdEntry(LinkedHashMap<String, Integer> map, String id, int stackSize) {
        ResourceLocation rl = ResourceLocation.parse(id);
        if (BuiltInRegistries.ITEM.containsKey(rl)) {
            map.put(id, stackSize);
        }
    }
}
