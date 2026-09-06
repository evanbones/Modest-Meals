package com.evandev.modest_meals.datagen;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.registry.ModItems;
import com.evandev.modest_meals.tag.ModItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {

    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                              CompletableFuture<TagLookup<net.minecraft.world.level.block.Block>> blockTags,
                              ExistingFileHelper existingFileHelper) {
        super(output, registries, blockTags, Constants.MOD_ID, existingFileHelper);
    }

    private static ResourceLocation convention(String path) {
        return ResourceLocation.fromNamespaceAndPath("c", path);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
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
        notAnIngredient.addOptionalTag(ResourceLocation.fromNamespaceAndPath("farmersdelight", "meals"));
        notAnIngredient.addOptionalTag(ResourceLocation.fromNamespaceAndPath("farmersdelight", "pies"));
        notAnIngredient.addOptionalTag(ResourceLocation.fromNamespaceAndPath("farmersdelight", "sweets"));
        notAnIngredient.addOptionalTag(ResourceLocation.fromNamespaceAndPath("farmersdelight", "snacks"));
    }
}
