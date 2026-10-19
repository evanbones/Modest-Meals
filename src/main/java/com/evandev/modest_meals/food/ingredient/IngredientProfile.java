package com.evandev.modest_meals.food.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * What one item is worth: how much it restores when eaten, and what it contributes to a dish cooked from it.
 * <p>
 * An empty health or stamina defers to the item's FoodProfile rate.
 */
public record IngredientProfile(
        Optional<Float> health,
        Optional<Float> stamina,
        Optional<Integer> digestTicks,
        Optional<Float> eatSeconds,
        Optional<ResourceLocation> effect,
        int potency,
        float temporaryHealth,
        int timeBonusSeconds
) {
    public static final IngredientProfile EMPTY = new IngredientProfile(
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), 0, 0.0F, 0);

    public static final MapCodec<IngredientProfile> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("health").forGetter(IngredientProfile::health),
            Codec.FLOAT.optionalFieldOf("stamina").forGetter(IngredientProfile::stamina),
            Codec.INT.optionalFieldOf("digest_ticks").forGetter(IngredientProfile::digestTicks),
            Codec.FLOAT.optionalFieldOf("eat_seconds").forGetter(IngredientProfile::eatSeconds),
            ResourceLocation.CODEC.optionalFieldOf("effect").forGetter(IngredientProfile::effect),
            Codec.INT.optionalFieldOf("potency", 0).forGetter(IngredientProfile::potency),
            Codec.FLOAT.optionalFieldOf("temporary_health", 0.0F).forGetter(IngredientProfile::temporaryHealth),
            Codec.INT.optionalFieldOf("time_bonus", 0).forGetter(IngredientProfile::timeBonusSeconds)
    ).apply(instance, IngredientProfile::new));

    public static final Codec<IngredientProfile> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientProfile> STREAM_CODEC =
            ByteBufCodecs.fromCodec(CODEC).cast();

    public IngredientProfile withDerived(float derivedHealth, float derivedStamina, int derivedDigestTicks) {
        return new IngredientProfile(
                Optional.of(health.orElse(derivedHealth)),
                Optional.of(stamina.orElse(derivedStamina)),
                Optional.of(digestTicks.orElse(derivedDigestTicks)),
                eatSeconds, effect, potency, temporaryHealth, timeBonusSeconds);
    }

    public float healthOrZero() {
        return health.orElse(0.0F);
    }

    public float staminaOrZero() {
        return stamina.orElse(0.0F);
    }

    public int digestTicksOrZero() {
        return digestTicks.orElse(0);
    }

    public float eatSecondsOrZero() {
        return eatSeconds.orElse(0.0F);
    }

    public boolean hasEffect() {
        return effect.isPresent() && potency > 0;
    }
}
