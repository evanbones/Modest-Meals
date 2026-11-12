package com.evandev.modest_meals.datagen;

//? if neoforge_legacy {
import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.compat.cold_sweat.ColdSweatCompat;
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

    private static final int MID = 50;
    private static final int TOP = 80;

    private static final float COMBAT = 0.5F;
    private static final float MOBILITY = 1.0F;
    private static final float UTILITY = 2.0F;

    public MealEffectProvider(PackOutput output) {
        super(output, "modest_meals/meal_effects", MealEffect.CODEC, "Modest Meals meal effects");
    }

    private static ResourceLocation idOf(Holder<MobEffect> effect) {
        return BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
    }

    @Override
    protected void collect(Map<ResourceLocation, MealEffect> entries) {
        threeTier(entries, "strength", MobEffects.DAMAGE_BOOST, 20, COMBAT);
        singleTier(entries, "resistance", MobEffects.DAMAGE_RESISTANCE, 20, COMBAT);
        threeTier(entries, "speed", MobEffects.MOVEMENT_SPEED, 30, MOBILITY);
        threeTier(entries, "haste", MobEffects.DIG_SPEED, 30, MOBILITY);
        threeTier(entries, "jump_boost", MobEffects.JUMP, 30, MOBILITY);

        singleTier(entries, "fire_resistance", MobEffects.FIRE_RESISTANCE, 70, UTILITY);
        singleTier(entries, "water_breathing", MobEffects.WATER_BREATHING, 70, UTILITY);
        singleTier(entries, "night_vision", MobEffects.NIGHT_VISION, 60, UTILITY);
        singleTier(entries, "glowing", MobEffects.GLOWING, 60, UTILITY);
        singleTier(entries, "invisibility", MobEffects.INVISIBILITY, 60, MOBILITY);
        singleTier(entries, "luck", MobEffects.LUCK, 60, UTILITY);

        thermal(entries, "cold_resistance", 70);
        thermal(entries, "heat_resistance", 70);

        modded(entries, "climbing", "neapolitan", "agility", 45, MOBILITY);
        modded(entries, "harmony", "neapolitan", "harmony", 40, UTILITY);
    }

    private void modded(Map<ResourceLocation, MealEffect> entries, String name,
                        String namespace, String path, int baseSeconds, float fillerScale) {
        put(entries, name, Optional.of(ResourceLocation.fromNamespaceAndPath(namespace, path)), baseSeconds, fillerScale,
                List.of(new MealEffect.Tier(0, 0)), List.of(), List.of());
    }

    private void thermal(Map<ResourceLocation, MealEffect> entries, String name, int baseSeconds) {
        put(entries, name, Optional.of(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name)), baseSeconds,
                UTILITY, List.of(
                        new MealEffect.Tier(0, 0),
                        new MealEffect.Tier(MID, 1)
                ),
                List.of(),
                List.of(ThermooCompat.MOD_ID, ColdSweatCompat.MOD_ID));
    }

    private void threeTier(Map<ResourceLocation, MealEffect> entries, String name,
                           Holder<MobEffect> effect, int baseSeconds, float fillerScale) {
        put(entries, name, Optional.of(idOf(effect)), baseSeconds, fillerScale, List.of(
                new MealEffect.Tier(0, 0),
                new MealEffect.Tier(MID, 1),
                new MealEffect.Tier(TOP, 2)
        ), List.of(), List.of());
    }

    private void singleTier(Map<ResourceLocation, MealEffect> entries, String name,
                            Holder<MobEffect> effect, int baseSeconds, float fillerScale) {
        put(entries, name, Optional.of(idOf(effect)), baseSeconds, fillerScale, List.of(
                new MealEffect.Tier(0, 0)
        ), List.of(), List.of());
    }

    private void put(Map<ResourceLocation, MealEffect> entries, String name, Optional<ResourceLocation> mobEffect,
                     int baseSeconds, float fillerScale, List<MealEffect.Tier> tiers, List<String> requiredMods,
                     List<String> anyOfMods) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
        entries.put(id, new MealEffect(id, mobEffect, baseSeconds, fillerScale, tiers,
                requiredMods, anyOfMods));
    }
}
//?}
