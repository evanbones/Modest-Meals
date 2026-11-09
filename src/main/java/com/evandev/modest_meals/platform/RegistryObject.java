package com.evandev.modest_meals.platform;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public record RegistryObject<T>(ResourceLocation id, Holder<T> holder) implements Supplier<T> {
    @Override
    public T get() {
        return holder.value();
    }
}
