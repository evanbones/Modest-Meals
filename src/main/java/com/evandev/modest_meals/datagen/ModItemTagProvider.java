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
    private static final TagKey<Item> TORTILLAS = itemTag("c", "foods/tortilla");
    private static final TagKey<Item> TOMATO_SAUCES = itemTag("c", "foods/tomato_sauce");
    private static final TagKey<Item> DOUGHS = itemTag("c", "foods/dough");

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
    }

    private void seedConventionFoods() {
        tag(TORTILLAS).addOptional(cd("tortilla"));
        tag(TOMATO_SAUCES).addOptional(fd("tomato_sauce"));
        tag(DOUGHS).addOptional(cd("corn_dough"));
    }
}
