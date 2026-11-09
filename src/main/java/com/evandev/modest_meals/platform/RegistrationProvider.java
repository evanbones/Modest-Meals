package com.evandev.modest_meals.platform;

import com.evandev.modest_meals.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

//? if neoforge {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
*///?} else {
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.registries.BuiltInRegistries;
//?}

public class RegistrationProvider<T> {
    private final ResourceKey<? extends Registry<T>> registryKey;
    //? if neoforge {
    /*private static final List<DeferredRegister<?>> REGISTERS = new ArrayList<>();
    private final DeferredRegister<T> deferredRegister;
    *///?} else {
    private Registry<T> registry;
    //?}

    private RegistrationProvider(ResourceKey<? extends Registry<T>> registryKey) {
        this.registryKey = registryKey;
        //? if neoforge {
        /*this.deferredRegister = DeferredRegister.create(registryKey, Constants.MOD_ID);
        REGISTERS.add(this.deferredRegister);
        *///?}
    }

    public static <T> RegistrationProvider<T> get(ResourceKey<? extends Registry<T>> registryKey) {
        return new RegistrationProvider<>(registryKey);
    }

    //? if neoforge {
    /*public static void registerAll(IEventBus modEventBus) {
        REGISTERS.forEach(register -> register.register(modEventBus));
    }

    @SuppressWarnings("unchecked")
    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
        DeferredHolder<T, I> holder = deferredRegister.register(name, supplier);
        return new RegistryObject<>(holder.getId(), (Holder<I>) (Holder<?>) holder);
    }

    public Registry<T> createSyncedRegistry() {
        return deferredRegister.makeRegistry(builder -> builder.sync(true));
    }
    *///?} else {
    @SuppressWarnings("unchecked")
    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
        Holder<T> holder = Registry.registerForHolder(registry(), id, supplier.get());
        return new RegistryObject<>(id, (Holder<I>) holder);
    }

    @SuppressWarnings("unchecked")
    public Registry<T> createSyncedRegistry() {
        //? if >=26.1 {
        /*this.registry = FabricRegistryBuilder.create((ResourceKey<Registry<T>>) registryKey)
        *///?} else
        this.registry = FabricRegistryBuilder.createSimple((ResourceKey<Registry<T>>) registryKey)
                .attribute(RegistryAttribute.SYNCED)
                .buildAndRegister();
        return this.registry;
    }

    @SuppressWarnings("unchecked")
    private Registry<T> registry() {
        if (registry == null) {
            //? if >=26.1 {
            /*registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
            *///?} else
            registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
        }
        return registry;
    }
    //?}
}
