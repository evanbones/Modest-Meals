package com.evandev.modest_meals.food.ingredient;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Stream;

public record MealLayerVisual(
        List<ResourceLocation> slots,
        Map<String, ResourceLocation> named
) {
    public static final MealLayerVisual EMPTY = new MealLayerVisual(List.of(), Map.of());

    public static final Codec<MealLayerVisual> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("slots", List.of()).forGetter(MealLayerVisual::slots),
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).optionalFieldOf("named", Map.of()).forGetter(MealLayerVisual::named)
    ).apply(instance, MealLayerVisual::new));

    public static final Codec<MealLayerVisual> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<MealLayerVisual, T>> decode(DynamicOps<T> ops, T input) {
            Optional<String> str = ops.getStringValue(input).result();
            if (str.isPresent()) {
                ResourceLocation loc = ResourceLocation.parse(str.get());
                return DataResult.success(Pair.of(MealLayerVisual.single(loc), input));
            }

            Optional<Stream<T>> stream = ops.getStream(input).result();
            if (stream.isPresent()) {
                List<ResourceLocation> list = stream.get()
                        .map(t -> ops.getStringValue(t).result().map(ResourceLocation::parse).orElse(null))
                        .filter(Objects::nonNull)
                        .toList();
                return DataResult.success(Pair.of(MealLayerVisual.slots(list), input));
            }

            Optional<MapLike<T>> map = ops.getMap(input).result();
            if (map.isPresent()) {
                MapLike<T> m = map.get();
                if (m.get("slots") != null) {
                    return OBJECT_CODEC.decode(ops, input);
                }
                Map<String, ResourceLocation> named = new LinkedHashMap<>();
                m.entries().forEach(pair -> {
                    String k = ops.getStringValue(pair.getFirst()).result().orElse("");
                    String v = ops.getStringValue(pair.getSecond()).result().orElse("");
                    if (!k.isEmpty() && !v.isEmpty()) {
                        named.put(k, ResourceLocation.parse(v));
                    }
                });
                return DataResult.success(Pair.of(MealLayerVisual.named(named), input));
            }

            return DataResult.error(() -> "Not a valid MealLayerVisual: " + input);
        }

        @Override
        public <T> DataResult<T> encode(MealLayerVisual input, DynamicOps<T> ops, T prefix) {
            if (input.named().size() == 1 && input.named().containsKey("sprite") && input.slots().size() <= 1) {
                return ResourceLocation.CODEC.encode(input.named().get("sprite"), ops, prefix);
            }
            if (input.slots().isEmpty() && !input.named().isEmpty()) {
                return Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).encode(input.named(), ops, prefix);
            }
            if (!input.slots().isEmpty() && input.named().isEmpty()) {
                return ResourceLocation.CODEC.listOf().encode(input.slots(), ops, prefix);
            }
            return OBJECT_CODEC.encode(input, ops, prefix);
        }
    };

    public MealLayerVisual {
        slots = List.copyOf(slots);
        named = Map.copyOf(named);
    }

    public static MealLayerVisual single(ResourceLocation sprite) {
        return new MealLayerVisual(List.of(sprite), Map.of("sprite", sprite));
    }

    public static MealLayerVisual slots(List<ResourceLocation> slots) {
        return new MealLayerVisual(slots, Map.of());
    }

    public static MealLayerVisual slots(ResourceLocation... slots) {
        return new MealLayerVisual(List.of(slots), Map.of());
    }

    public static MealLayerVisual named(Map<String, ResourceLocation> named) {
        return new MealLayerVisual(List.of(), named);
    }

    public static MealLayerVisual triplet(ResourceLocation base) {
        ResourceLocation s0 = base.withSuffix("_0");
        ResourceLocation s1 = base.withSuffix("_1");
        ResourceLocation s2 = base.withSuffix("_2");
        return slots(s0, s1, s2);
    }

    public Optional<ResourceLocation> getSprite(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < slots.size()) {
            return Optional.of(slots.get(slotIndex));
        }
        String indexStr = String.valueOf(slotIndex);
        if (named.containsKey(indexStr)) {
            return Optional.of(named.get(indexStr));
        }
        if (slotIndex == 0 && named.containsKey("left")) {
            return Optional.of(named.get("left"));
        }
        if (slotIndex == 1 && named.containsKey("middle")) {
            return Optional.of(named.get("middle"));
        }
        if (slotIndex == 2 && named.containsKey("right")) {
            return Optional.of(named.get("right"));
        }
        if (named.containsKey("sprite")) {
            return Optional.of(named.get("sprite"));
        }
        if (slots.size() == 1) {
            return Optional.of(slots.getFirst());
        }
        if (named.size() == 1) {
            return Optional.of(named.values().iterator().next());
        }
        return Optional.empty();
    }
}
