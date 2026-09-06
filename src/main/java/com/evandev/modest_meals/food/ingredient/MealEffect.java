package com.evandev.modest_meals.food.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * One axis a meal's effect can fall on, identified by (and named after) the mob effect it grants.
 */
public record MealEffect(
        ResourceLocation id,
        Optional<ResourceLocation> mobEffect,
        int baseSeconds,
        List<Tier> tiers,
        Optional<String> namePrefixKey
) {
    public static final Codec<MealEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(MealEffect::id),
            ResourceLocation.CODEC.optionalFieldOf("mob_effect").forGetter(MealEffect::mobEffect),
            Codec.INT.optionalFieldOf("base_seconds", 30).forGetter(MealEffect::baseSeconds),
            Tier.CODEC.listOf().optionalFieldOf("tiers", List.of(new Tier(0, 0))).forGetter(MealEffect::tiers),
            Codec.STRING.optionalFieldOf("name_prefix_key").forGetter(MealEffect::namePrefixKey)
    ).apply(instance, MealEffect::new));

    public Optional<Integer> amplifierFor(int potency) {
        return tiers.stream()
                .filter(tier -> potency >= tier.threshold())
                .max(Comparator.comparingInt(Tier::threshold))
                .map(Tier::amplifier);
    }

    public Optional<Holder.Reference<MobEffect>> resolveMobEffect() {
        return mobEffect.flatMap(BuiltInRegistries.MOB_EFFECT::getHolder);
    }

    /**
     * Whether this axis can currently be applied to anything.
     */
    public boolean isUsable() {
        return resolveMobEffect().isPresent();
    }

    /**
     * The lang key for the adjective prepended to a meal's name, e.g. {@code modest_meals.meal_prefix.strength}.
     */
    public String namePrefixKeyOrDefault() {
        return namePrefixKey.orElseGet(() -> "modest_meals.meal_prefix." + id.getPath());
    }

    /**
     * The axis's display name: the mob effect's own name where there is one, otherwise our own key.
     */
    public Component displayName() {
        return resolveMobEffect()
                .map(holder -> (Component) Component.translatable(holder.value().getDescriptionId()))
                .orElseGet(() -> Component.translatable("modest_meals.meal_effect." + id.getPath()));
    }

    /**
     * A potency threshold and the mob-effect amplifier reaching it awards.
     */
    public record Tier(int threshold, int amplifier) {
        public static final Codec<Tier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("threshold").forGetter(Tier::threshold),
                Codec.INT.fieldOf("amplifier").forGetter(Tier::amplifier)
        ).apply(instance, Tier::new));
    }
}
