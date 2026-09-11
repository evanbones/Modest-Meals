package com.evandev.modest_meals.food.ingredient;

import com.evandev.modest_meals.Constants;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Visual configuration for an ingredient cooked into dynamic meals.
 */
public record MealIngredientVisual(
        Map<ResourceLocation, MealLayerVisual> meals
) {
    private static final Codec<MealIngredientVisual> STRICT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, MealLayerVisual.CODEC)
                    .optionalFieldOf("meals", Map.of()).forGetter(MealIngredientVisual::meals)
    ).apply(instance, MealIngredientVisual::new));

    public static final Codec<MealIngredientVisual> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<MealIngredientVisual, T>> decode(DynamicOps<T> ops, T input) {
            Optional<MapLike<T>> mapOpt = ops.getMap(input).result();
            if (mapOpt.isEmpty()) {
                return STRICT_CODEC.decode(ops, input);
            }

            MapLike<T> map = mapOpt.get();
            Map<ResourceLocation, MealLayerVisual> meals = new LinkedHashMap<>();

            T mealsNode = map.get("meals");
            if (mealsNode != null) {
                DataResult<Map<ResourceLocation, MealLayerVisual>> r =
                        Codec.unboundedMap(ResourceLocation.CODEC, MealLayerVisual.CODEC).parse(ops, mealsNode);
                r.result().ifPresent(meals::putAll);
            }

            map.entries().forEach(pair -> {
                String key = ops.getStringValue(pair.getFirst()).result().orElse("");
                if (!key.equals("item") && !key.equals("tag") && !key.equals("target")
                        && !key.equals("priority") && !key.equals("meals")) {
                    ResourceLocation mealId = key.contains(":")
                            ? ResourceLocation.parse(key)
                            : ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, key);
                    DataResult<MealLayerVisual> r = MealLayerVisual.CODEC.parse(ops, pair.getSecond());
                    r.result().ifPresent(visual -> meals.put(mealId, visual));
                }
            });

            return DataResult.success(Pair.of(new MealIngredientVisual(meals), input));
        }

        @Override
        public <T> DataResult<T> encode(MealIngredientVisual input, DynamicOps<T> ops, T prefix) {
            return STRICT_CODEC.encode(input, ops, prefix);
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, MealIngredientVisual> STREAM_CODEC =
            ByteBufCodecs.<MealIngredientVisual>fromCodec(CODEC).cast();

    public MealIngredientVisual {
        meals = Map.copyOf(meals);
    }

    public boolean supportsMeal(ResourceLocation mealTypeId) {
        if (meals.containsKey(mealTypeId)) {
            return true;
        }
        for (ResourceLocation id : meals.keySet()) {
            if (id.getPath().equals(mealTypeId.getPath())) {
                return true;
            }
        }
        return false;
    }

    public Optional<ResourceLocation> getSprite(ResourceLocation mealTypeId, int slotIndex, int totalSlots) {
        MealLayerVisual visual = meals.get(mealTypeId);
        if (visual == null) {
            for (Map.Entry<ResourceLocation, MealLayerVisual> entry : meals.entrySet()) {
                if (entry.getKey().getPath().equals(mealTypeId.getPath())) {
                    visual = entry.getValue();
                    break;
                }
            }
        }
        if (visual != null) {
            return visual.getSprite(slotIndex);
        }
        return Optional.empty();
    }
}
