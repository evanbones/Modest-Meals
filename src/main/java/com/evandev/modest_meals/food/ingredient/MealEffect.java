package com.evandev.modest_meals.food.ingredient;

import com.evandev.modest_meals.platform.Platform;
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

public record MealEffect(
        ResourceLocation id,
        Optional<ResourceLocation> mobEffect,
        int baseSeconds,
        float fillerScale,
        List<Tier> tiers,
        List<String> requiredMods,
        List<String> anyOfMods
) {
    public static final Codec<MealEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(MealEffect::id),
            ResourceLocation.CODEC.optionalFieldOf("mob_effect").forGetter(MealEffect::mobEffect),
            Codec.INT.optionalFieldOf("base_seconds", 30).forGetter(MealEffect::baseSeconds),
            Codec.FLOAT.optionalFieldOf("filler_scale", 1.0F).forGetter(MealEffect::fillerScale),
            Tier.CODEC.listOf().optionalFieldOf("tiers", List.of(new Tier(0, 0))).forGetter(MealEffect::tiers),
            Codec.STRING.listOf().optionalFieldOf("required_mods", List.of()).forGetter(MealEffect::requiredMods),
            Codec.STRING.listOf().optionalFieldOf("any_of_mods", List.of()).forGetter(MealEffect::anyOfMods)
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

    public boolean isUsable() {
        return resolveMobEffect().isPresent() && requiredModsPresent() && anyOfModsPresent();
    }

    private boolean requiredModsPresent() {
        return requiredMods.stream().allMatch(modId -> Platform.isModLoaded(modId));
    }

    private boolean anyOfModsPresent() {
        return anyOfMods.isEmpty() || anyOfMods.stream().anyMatch(modId -> Platform.isModLoaded(modId));
    }


    public Component displayName() {
        return resolveMobEffect()
                .map(holder -> (Component) Component.translatable(holder.value().getDescriptionId()))
                .orElseGet(() -> Component.translatable("modest_meals.meal_effect." + id.getPath()));
    }

    public record Tier(int threshold, int amplifier) {
        public static final Codec<Tier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("threshold").forGetter(Tier::threshold),
                Codec.INT.fieldOf("amplifier").forGetter(Tier::amplifier)
        ).apply(instance, Tier::new));
    }
}
