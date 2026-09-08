package com.evandev.modest_meals.datagen;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.compat.thermoo.ThermooCompat;
import com.evandev.modest_meals.food.ingredient.MealEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MealEffectProvider extends JsonDataProvider<MealEffect> {

    private static final int MID = 30;
    private static final int TOP = 45;

    public MealEffectProvider(PackOutput output) {
        super(output, "modest_meals/meal_effects", MealEffect.CODEC, "Modest Meals meal effects");
    }

    private static ResourceLocation idOf(Holder<MobEffect> effect) {
        return BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
    }

    @Override
    protected void collect(Map<ResourceLocation, MealEffect> entries) {
        threeTier(entries, "strength", MobEffects.DAMAGE_BOOST, 20);
        twoTier(entries, "resistance", MobEffects.DAMAGE_RESISTANCE, 20);
        threeTier(entries, "speed", MobEffects.MOVEMENT_SPEED, 30);
        threeTier(entries, "haste", MobEffects.DIG_SPEED, 30);
        threeTier(entries, "jump_boost", MobEffects.JUMP, 30);

        singleTier(entries, "fire_resistance", MobEffects.FIRE_RESISTANCE, 70);
        singleTier(entries, "water_breathing", MobEffects.WATER_BREATHING, 70);
        singleTier(entries, "night_vision", MobEffects.NIGHT_VISION, 60);
        singleTier(entries, "glowing", MobEffects.GLOWING, 60);
        singleTier(entries, "invisibility", MobEffects.INVISIBILITY, 60);
        singleTier(entries, "luck", MobEffects.LUCK, 60);

        thermal(entries, "cold_resistance", 70);
        thermal(entries, "heat_resistance", 70);

        modded(entries, "climbing", "neapolitan", "agility", 45);
        modded(entries, "harmony", "neapolitan", "harmony", 40);
    }

    private void modded(Map<ResourceLocation, MealEffect> entries, String name,
                        String namespace, String path, int baseSeconds) {
        put(entries, name, Optional.of(ResourceLocation.fromNamespaceAndPath(namespace, path)), baseSeconds,
                List.of(new MealEffect.Tier(0, 0)), List.of());
    }

    private void thermal(Map<ResourceLocation, MealEffect> entries, String name, int baseSeconds) {
        put(entries, name, Optional.of(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name)), baseSeconds,
                List.of(
                        new MealEffect.Tier(0, 0),
                        new MealEffect.Tier(MID, 1)
                ),
                List.of(ThermooCompat.MOD_ID));
    }

    private void threeTier(Map<ResourceLocation, MealEffect> entries, String name,
                           Holder<MobEffect> effect, int baseSeconds) {
        put(entries, name, Optional.of(idOf(effect)), baseSeconds, List.of(
                new MealEffect.Tier(0, 0),
                new MealEffect.Tier(MID, 1),
                new MealEffect.Tier(TOP, 2)
        ), List.of());
    }

    private void twoTier(Map<ResourceLocation, MealEffect> entries, String name,
                         Holder<MobEffect> effect, int baseSeconds) {
        put(entries, name, Optional.of(idOf(effect)), baseSeconds, List.of(
                new MealEffect.Tier(0, 0),
                new MealEffect.Tier(MID, 1)
        ), List.of());
    }

    private void singleTier(Map<ResourceLocation, MealEffect> entries, String name,
                            Holder<MobEffect> effect, int baseSeconds) {
        put(entries, name, Optional.of(idOf(effect)), baseSeconds, List.of(
                new MealEffect.Tier(0, 0)
        ), List.of());
    }

    private void put(Map<ResourceLocation, MealEffect> entries, String name, Optional<ResourceLocation> mobEffect,
                     int baseSeconds, List<MealEffect.Tier> tiers, List<String> requiredMods) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
        entries.put(id, new MealEffect(id, mobEffect, baseSeconds, tiers,
                Optional.of("modest_meals.meal_prefix." + name), requiredMods));
    }
}
