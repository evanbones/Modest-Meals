package com.evandev.modest_meals.registry;

import com.evandev.modest_meals.food.meal.MealTypeManager;
import com.evandev.modest_meals.item.MealItem;
import com.evandev.modest_meals.platform.RegistrationProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ModCreativeTabs {
    private static final RegistrationProvider<CreativeModeTab> CREATIVE_TABS =
            RegistrationProvider.get(Registries.CREATIVE_MODE_TAB);

    public static final Supplier<CreativeModeTab> MAIN = CREATIVE_TABS.register("main", () ->
            //? if neoforge {
            CreativeModeTab.builder()
            //?} else if >=26.1 {
            /*net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab.builder()
            *///?} else
            //net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder()
            .title(Component.translatable("itemGroup.modest_meals"))
            .icon(() -> new ItemStack(ModItems.SANDWICH.get()))
            .displayItems((params, output) -> {
                ModItems.CREATIVE_TAB_ITEMS.forEach(itemSupplier -> {
                    Item item = itemSupplier.get();
                    output.accept(new ItemStack(item));

                    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                    boolean isDubiousSupported = MealTypeManager.get(itemId)
                            .map(type -> type.requiresSupportedIngredients() || type.dubiousSprite().isPresent())
                            .orElse(false);
                    if (isDubiousSupported) {
                        output.accept(MealItem.makeDubious(item));
                    }
                });
            })
            .build());

    public static void init() {
    }
}
