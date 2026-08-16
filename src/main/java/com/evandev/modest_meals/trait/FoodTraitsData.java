package com.evandev.modest_meals.trait;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Objects;

public record FoodTraitsData(ImmutableList<FoodTrait> traits) {
    public static final Codec<FoodTraitsData> CODEC = FoodTraitType.CODEC.listOf()
            .xmap(FoodTraitsData::new, FoodTraitsData::traits);
    public static final StreamCodec<RegistryFriendlyByteBuf, FoodTraitsData> STREAM_CODEC = FoodTraitType.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(FoodTraitsData::new, FoodTraitsData::traits);

    public FoodTraitsData(List<FoodTrait> traits) {
        this(ImmutableList.copyOf(traits));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FoodTraitsData(ImmutableList<FoodTrait> traits1))) return false;
        return Objects.equals(this.traits, traits1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traits);
    }
}
