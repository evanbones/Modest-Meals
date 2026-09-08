package com.evandev.modest_meals.effect;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.attribute.ModAttributes;
import com.evandev.modest_meals.compat.thermoo.ThermooCompat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Constants.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> STAMINA_NOURISHMENT = MOB_EFFECTS.register(
            "stamina_nourishment",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xF3B300) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return false;
                }
            }
    );

    public static final DeferredHolder<MobEffect, MobEffect> STAMINA_BOOST = MOB_EFFECTS.register(
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

    public static final DeferredHolder<MobEffect, MobEffect> STAMINA_DEPLETION = MOB_EFFECTS.register(
            "stamina_depletion",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xAA0000) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return false;
                }
            }
    );

    public static final DeferredHolder<MobEffect, MobEffect> STAMINA_NO_REGEN = MOB_EFFECTS.register(
            "stamina_no_regen",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x555555) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return false;
                }
            }
    );

    public static final DeferredHolder<MobEffect, MobEffect> HEALTH_NO_REGEN = MOB_EFFECTS.register(
            "health_no_regen",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x880000) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return false;
                }
            }
    );

    // TODO: these should also probably be configurable
    private static final double THERMAL_RESISTANCE_PER_LEVEL = 2.0;
    private static final double ENVIRONMENT_RESISTANCE_PER_LEVEL = 0.5;

    public static final DeferredHolder<MobEffect, MobEffect> COLD_RESISTANCE = MOB_EFFECTS.register(
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

    public static final DeferredHolder<MobEffect, MobEffect> HEAT_RESISTANCE = MOB_EFFECTS.register(
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

    public static void register(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
    }
}
