package com.evandev.modest_meals.food.meal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Comparator;
import java.util.List;

/**
 * The global constants the meal assembler works from. Datapack-supplied.
 *
 * @param secondsPerIngredient  flat duration every ingredient adds, regardless of what it does
 * @param maxDurationSeconds    ceiling on any effect duration
 * @param temporaryStaminaSteps points-to-half-bolts steps for temporary stamina
 * @param eatTimeSteps          how long a dish takes to eat, by how much it restores
 */
public record MealFormula(
        int secondsPerIngredient,
        int maxDurationSeconds,
        List<Step> temporaryStaminaSteps,
        List<EatTime> eatTimeSteps
) {
    public static final MealFormula DEFAULT = new MealFormula(
            30,
            1800,
            List.of(
                    new Step(1, 2.0F),
                    new Step(2, 4.0F),
                    new Step(3, 7.0F),
                    new Step(4, 10.0F),
                    new Step(6, 15.0F),
                    new Step(8, 20.0F),
                    new Step(9, 24.0F),
                    new Step(11, 28.0F)
            ),
            List.of(
                    new EatTime(0.0F, 0.8F),
                    new EatTime(4.0F, 1.6F),
                    new EatTime(16.0F, 2.4F)
            )
    );

    public static final Codec<MealFormula> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("seconds_per_ingredient", DEFAULT.secondsPerIngredient())
                    .forGetter(MealFormula::secondsPerIngredient),
            Codec.INT.optionalFieldOf("max_duration_seconds", DEFAULT.maxDurationSeconds())
                    .forGetter(MealFormula::maxDurationSeconds),
            Step.CODEC.listOf().optionalFieldOf("temporary_stamina_steps", DEFAULT.temporaryStaminaSteps())
                    .forGetter(MealFormula::temporaryStaminaSteps),
            EatTime.CODEC.listOf().optionalFieldOf("eat_time_steps", DEFAULT.eatTimeSteps())
                    .forGetter(MealFormula::eatTimeSteps)
    ).apply(instance, MealFormula::new));

    public static final Codec<MealFormula> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("seconds_per_ingredient").forGetter(MealFormula::secondsPerIngredient),
            Codec.INT.fieldOf("max_duration_seconds").forGetter(MealFormula::maxDurationSeconds),
            Step.CODEC.listOf().fieldOf("temporary_stamina_steps").forGetter(MealFormula::temporaryStaminaSteps),
            EatTime.CODEC.listOf().fieldOf("eat_time_steps").forGetter(MealFormula::eatTimeSteps)
    ).apply(instance, MealFormula::new));

    /**
     * How long a dish restoring this much takes to eat, in seconds.
     *
     * @param portion the dish's health plus stamina, in half-hearts and half-bolts
     */
    public float eatSecondsFor(float portion) {
        return eatTimeSteps.stream()
                .filter(step -> portion >= step.portion())
                .max(Comparator.comparingDouble(EatTime::portion))
                .map(EatTime::seconds)
                .orElse(1.6F);
    }

    /**
     * Half-bolts of temporary stamina for a given points total, taking the highest step it reaches.
     */
    public float temporaryStaminaFor(float points) {
        return temporaryStaminaSteps.stream()
                .filter(step -> points >= step.points())
                .max(Comparator.comparingInt(Step::points))
                .map(Step::stamina)
                .orElse(0.0F);
    }

    /**
     * A portion size and how many seconds a dish that big takes to eat.
     */
    public record EatTime(float portion, float seconds) {
        public static final Codec<EatTime> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("portion").forGetter(EatTime::portion),
                Codec.FLOAT.fieldOf("seconds").forGetter(EatTime::seconds)
        ).apply(instance, EatTime::new));
    }

    public record Step(int points, float stamina) {
        public static final Codec<Step> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("points").forGetter(Step::points),
                Codec.FLOAT.fieldOf("stamina").forGetter(Step::stamina)
        ).apply(instance, Step::new));
    }
}
