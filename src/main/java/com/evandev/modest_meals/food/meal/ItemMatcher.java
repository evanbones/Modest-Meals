package com.evandev.modest_meals.food.meal;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record ItemMatcher(List<Entry> entries) implements Predicate<ItemStack> {
    private static final String COMPONENTS_TYPE = "neoforge:components";

    private static final Codec<Entry> ENTRY_CODEC = Codec.PASSTHROUGH.comapFlatMap(ItemMatcher::decodeEntry, ItemMatcher::encodeEntry);

    public static final Codec<ItemMatcher> CODEC = Codec.either(ENTRY_CODEC, ENTRY_CODEC.listOf()).xmap(
            either -> either.map(entry -> new ItemMatcher(List.of(entry)), ItemMatcher::new),
            matcher -> matcher.entries().size() == 1
                    ? Either.left(matcher.entries().getFirst())
                    : Either.right(matcher.entries()));

    public static ItemMatcher item(Item item) {
        return new ItemMatcher(List.of(new ItemEntry(item)));
    }

    public static ItemMatcher tag(TagKey<Item> tag) {
        return new ItemMatcher(List.of(new TagEntry(tag)));
    }

    @SafeVarargs
    public static ItemMatcher tags(TagKey<Item>... tags) {
        return new ItemMatcher(Stream.of(tags).<Entry>map(TagEntry::new).toList());
    }

    public static <T> ItemMatcher withComponent(Item item, DataComponentType<T> type, T value) {
        return new ItemMatcher(List.of(new ComponentsEntry(item, DataComponentPatch.builder().set(type, value).build())));
    }

    public ItemMatcher or(ItemMatcher other) {
        List<Entry> combined = new ArrayList<>(entries);
        combined.addAll(other.entries);
        return new ItemMatcher(combined);
    }

    @Override
    public boolean test(ItemStack stack) {
        return !stack.isEmpty() && entries.stream().anyMatch(entry -> entry.test(stack));
    }

    public List<ItemStack> displayStacks() {
        return entries.stream().flatMap(Entry::displayStacks).toList();
    }

    private static DataResult<Entry> decodeEntry(Dynamic<?> dynamic) {
        Optional<String> asString = dynamic.asString().result();
        if (asString.isPresent()) {
            String value = asString.get();
            return value.startsWith("#")
                    ? parseId(value.substring(1)).map(id -> new TagEntry(TagKey.create(Registries.ITEM, id)))
                    : parseItem(value).map(ItemEntry::new);
        }
        Optional<String> tag = dynamic.get("tag").asString().result();
        if (tag.isPresent()) {
            return parseId(tag.get()).map(id -> new TagEntry(TagKey.create(Registries.ITEM, id)));
        }
        Optional<String> item = dynamic.get("item").asString().result();
        if (item.isPresent()) {
            return parseItem(item.get()).map(ItemEntry::new);
        }
        Optional<String> items = dynamic.get("items").asString().result();
        if (items.isPresent()) {
            return parseItem(items.get()).flatMap(found -> DataComponentPatch.CODEC.parse(dynamic.get("components").orElseEmptyMap())
                    .map(patch -> new ComponentsEntry(found, patch)));
        }
        return DataResult.error(() -> "Expected an item, a tag or a components entry: " + dynamic.getValue());
    }

    private static Dynamic<?> encodeEntry(Entry entry) {
        JsonObject json = new JsonObject();
        switch (entry) {
            case ItemEntry itemEntry -> json.addProperty("item", BuiltInRegistries.ITEM.getKey(itemEntry.item()).toString());
            case TagEntry tagEntry -> json.addProperty("tag", tagEntry.tag().location().toString());
            case ComponentsEntry componentsEntry -> {
                json.addProperty("type", COMPONENTS_TYPE);
                json.add("components", DataComponentPatch.CODEC.encodeStart(JsonOps.INSTANCE, componentsEntry.components()).getOrThrow());
                json.addProperty("items", BuiltInRegistries.ITEM.getKey(componentsEntry.item()).toString());
            }
        }
        return new Dynamic<>(JsonOps.INSTANCE, json);
    }

    private static DataResult<ResourceLocation> parseId(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        return id == null ? DataResult.error(() -> "Invalid id: " + value) : DataResult.success(id);
    }

    private static DataResult<Item> parseItem(String value) {
        return parseId(value).flatMap(id -> BuiltInRegistries.ITEM.getOptional(id)
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "Unknown item: " + value)));
    }

    public sealed interface Entry permits ItemEntry, TagEntry, ComponentsEntry {
        boolean test(ItemStack stack);

        Stream<ItemStack> displayStacks();
    }

    public record ItemEntry(Item item) implements Entry {
        @Override
        public boolean test(ItemStack stack) {
            return stack.is(item);
        }

        @Override
        public Stream<ItemStack> displayStacks() {
            return Stream.of(new ItemStack(item));
        }
    }

    public record TagEntry(TagKey<Item> tag) implements Entry {
        @Override
        public boolean test(ItemStack stack) {
            return stack.is(tag);
        }

        @Override
        public Stream<ItemStack> displayStacks() {
            return BuiltInRegistries.ITEM.stream().filter(item -> item.builtInRegistryHolder().is(tag)).map(ItemStack::new);
        }
    }

    public record ComponentsEntry(Item item, DataComponentPatch components) implements Entry {
        @Override
        public boolean test(ItemStack stack) {
            if (!stack.is(item)) {
                return false;
            }
            for (TypedDataComponent<?> component : components.split().added()) {
                if (!Objects.equals(stack.get(component.type()), component.value())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Stream<ItemStack> displayStacks() {
            ItemStack stack = new ItemStack(item);
            stack.applyComponents(components);
            return Stream.of(stack);
        }
    }
}
