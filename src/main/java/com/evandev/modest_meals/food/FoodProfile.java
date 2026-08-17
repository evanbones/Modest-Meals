package com.evandev.modest_meals.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * A rule that determines what a food restores from its vanilla nutrition.
 */
public record FoodProfile(
        String id,
        String match,
        int priority,
        float healthPerNutrition,
        float healthTicksPerPoint,
        float staminaPerNutrition
) {
    public static final String MATCH_ANY = "*";

    public static final Codec<FoodProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(FoodProfile::id),
            Codec.STRING.fieldOf("match").forGetter(FoodProfile::match),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(FoodProfile::priority),
            Codec.FLOAT.optionalFieldOf("health_per_nutrition", 0.0F).forGetter(FoodProfile::healthPerNutrition),
            Codec.FLOAT.optionalFieldOf("health_ticks_per_point", 30.0F).forGetter(FoodProfile::healthTicksPerPoint),
            Codec.FLOAT.optionalFieldOf("stamina_per_nutrition", 0.0F).forGetter(FoodProfile::staminaPerNutrition)
    ).apply(instance, FoodProfile::new));

    /**
     * Health restored, in half-hearts, for a food of the given nutrition.
     */
    public float healthFor(int nutrition) {
        return healthPerNutrition * nutrition;
    }

    /**
     * How long that health takes to digest, in ticks.
     */
    public int digestTicksFor(float healthPoints) {
        return Math.max(0, (int) (healthPoints * healthTicksPerPoint));
    }

    /**
     * Stamina restored, in seconds of sprint time, for a food of the given nutrition.
     */
    public float staminaFor(int nutrition) {
        return staminaPerNutrition * nutrition;
    }
}
