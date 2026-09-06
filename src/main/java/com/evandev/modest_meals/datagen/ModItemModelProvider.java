package com.evandev.modest_meals.datagen;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Constants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ModItems.CREATIVE_TAB_ITEMS.forEach(this::flatItem);
    }

    private void flatItem(Supplier<Item> item) {
        String name = BuiltInRegistries.ITEM.getKey(item.get()).getPath();
        withExistingParent(name, ResourceLocation.withDefaultNamespace("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/" + name));
    }
}
