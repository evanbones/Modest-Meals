package com.evandev.modest_meals.effect;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.attribute.ModAttributes;
import com.evandev.modest_meals.compat.thermoo.ThermooCompat;
import com.evandev.modest_meals.platform.RegistrationProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.function.Supplier;

public class ModMobEffects {
    private static final RegistrationProvider<MobEffect> MOB_EFFECTS = RegistrationProvider.get(Registries.MOB_EFFECT);

    public static final Holder<MobEffect> STAMINA_NOURISHMENT = register(
            "stamina_nourishment",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xF3B300) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return false;
                }
            }
    );

    public static final Holder<MobEffect> STAMINA_BOOST = register(
            "stamina_boost",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xECD613) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return false;
                }
            }.addAttributeModifier(
                    ModAttributes.STAMINA_CAPACITY,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "effect.stamina_boost"),
                    4.0,
                    AttributeModifier.Operation.ADD_VALUE
            )
    );

    public static final Holder<MobEffect> STAMINA_DEPLETION = register(
            "stamina_depletion",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xAA0000) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return false;
                }
            }
    );

    public static final Holder<MobEffect> STAMINA_NO_REGEN = register(
            "stamina_no_regen",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x555555) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return false;
                }
            }
    );

    public static final Holder<MobEffect> HEALTH_NO_REGEN = register(
            "health_no_regen",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x880000) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return false;
                }
            }
    );

    // TODO: these should also probably be configurable
    private static final double THERMAL_RESISTANCE_PER_LEVEL = 2.5;
    private static final double ENVIRONMENT_RESISTANCE_PER_LEVEL = 1.0;

    public static final Holder<MobEffect> COLD_RESISTANCE = register(
            "cold_resistance",
            () -> new ThermalResistanceEffect(
                    "cold_resistance",
                    ThermooCompat::frostResistance,
                    ThermooCompat::environmentFrostResistance,
                    THERMAL_RESISTANCE_PER_LEVEL,
                    ENVIRONMENT_RESISTANCE_PER_LEVEL,
                    0xE08A3C
            )
    );

    public static final Holder<MobEffect> HEAT_RESISTANCE = register(
            "heat_resistance",
            () -> new ThermalResistanceEffect(
                    "heat_resistance",
                    ThermooCompat::heatResistance,
                    ThermooCompat::environmentHeatResistance,
                    THERMAL_RESISTANCE_PER_LEVEL,
                    ENVIRONMENT_RESISTANCE_PER_LEVEL,
                    0x6EC6E8
            )
    );

    private static Holder<MobEffect> register(String name, Supplier<MobEffect> effect) {
        return MOB_EFFECTS.register(name, effect).holder();
    }

    public static void init() {
    }
}
