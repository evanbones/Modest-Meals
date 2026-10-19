package com.evandev.modest_meals.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class JsonDataProvider<T> implements DataProvider {

    private final PackOutput.PathProvider pathProvider;
    private final Codec<T> codec;
    private final String name;

    protected JsonDataProvider(PackOutput output, String directory, Codec<T> codec, String name) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, directory);
        this.codec = codec;
        this.name = name;
    }

    protected abstract void collect(Map<ResourceLocation, T> entries);

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, T> entries = new LinkedHashMap<>();
        collect(entries);

        CompletableFuture<?>[] futures = entries.entrySet().stream()
                .map(entry -> {
                    Path path = pathProvider.json(entry.getKey());
                    JsonElement json = codec.encodeStart(JsonOps.INSTANCE, entry.getValue())
                            .getOrThrow(error -> new IllegalStateException(
                                    "Failed to encode " + entry.getKey() + ": " + error));
                    return DataProvider.saveStable(output, json, path);
                })
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return name;
    }
}
