package com.evandev.modest_meals.tag;

import com.evandev.modest_meals.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {
    public static final TagKey<Item> NOT_AN_INGREDIENT = tag("not_an_ingredient");
    public static final TagKey<Item> INGREDIENT_MEALS = tag("ingredient_meals");
    public static final TagKey<Item> JAM_FRUITS = tag("jam_fruits");
    public static final TagKey<Item> WHEATS = itemTag("c", "crops/wheat");
    public static final TagKey<Item> PIZZA_SAUCES = tag("pizza_sauces");
    public static final TagKey<Item> ROLL_FILLINGS = tag("roll_fillings");
    public static final TagKey<Item> NOT_FOR_DISHES = tag("not_for_dishes");
    public static final TagKey<Item> STAPLES = tag("staples");
    public static final TagKey<Item> NOT_FOR_JAM = tag("not_for_jam");
    public static final TagKey<Item> SWEETS = tag("sweets");
    public static final TagKey<Item> DESSERT_INGREDIENTS = tag("dessert_ingredients");
    public static final TagKey<Item> KNIVES = itemTag("c", "tools/knife");

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, path));
    }

    private static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
