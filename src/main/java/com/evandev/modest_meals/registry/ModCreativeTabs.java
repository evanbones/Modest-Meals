package com.evandev.modest_meals.registry;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import com.evandev.modest_meals.item.MealItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final Supplier<CreativeModeTab> MAIN = CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
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

    public static void register(IEventBus modBus) {
        CREATIVE_TABS.register(modBus);
    }
}
