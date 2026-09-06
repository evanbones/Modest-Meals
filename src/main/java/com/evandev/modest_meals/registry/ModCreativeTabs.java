package com.evandev.modest_meals.registry;

import com.evandev.modest_meals.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final Supplier<CreativeModeTab> MAIN = CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.modest_meals"))
            .icon(() -> new ItemStack(ModItems.STEW.get()))
            .displayItems((params, output) -> ModItems.CREATIVE_TAB_ITEMS.forEach(
                    item -> output.accept(new ItemStack(item.get()))))
            .build());

    public static void register(IEventBus modBus) {
        CREATIVE_TABS.register(modBus);
    }
}
