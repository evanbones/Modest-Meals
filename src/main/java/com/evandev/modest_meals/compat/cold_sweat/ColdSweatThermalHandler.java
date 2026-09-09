package com.evandev.modest_meals.compat.cold_sweat;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.effect.ModMobEffects;
import com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.Temperature.Trait;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class ColdSweatThermalHandler {

    private static final ResourceLocation MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "meal_resistance");

    private static final double RESISTANCE_PER_LEVEL = 0.5;
    private static final double DAMPENING_PER_LEVEL = 1.0;
    private static final int SYNC_INTERVAL = 20;

    private ColdSweatThermalHandler() {
    }

    @SubscribeEvent
    public static void registerTempModifiers(TempModifierRegisterEvent event) {
        event.register(MODIFIER_ID, MealResistanceTempModifier::new);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)
                || entity.level().isClientSide()
                || entity.tickCount % SYNC_INTERVAL != 0
                || !EntityTempManager.isTemperatureEnabled(entity)) {
            return;
        }

        sync(entity, ModMobEffects.COLD_RESISTANCE, Trait.COLD_RESISTANCE, Trait.COLD_DAMPENING);
        sync(entity, ModMobEffects.HEAT_RESISTANCE, Trait.HEAT_RESISTANCE, Trait.HEAT_DAMPENING);
    }

    private static void sync(LivingEntity entity, Holder<MobEffect> effect, Trait resistance, Trait dampening) {
        MobEffectInstance instance = entity.getEffect(effect);
        int level = instance == null ? 0 : instance.getAmplifier() + 1;
        apply(entity, resistance, level * RESISTANCE_PER_LEVEL);
        apply(entity, dampening, level * DAMPENING_PER_LEVEL);
    }

    private static void apply(LivingEntity entity, Trait trait, double amount) {
        double current = Temperature.getModifier(entity, trait, MealResistanceTempModifier.class)
                .map(MealResistanceTempModifier::getAmount)
                .orElse(0.0);
        if (current == amount) {
            return;
        }

        if (amount <= 0.0) {
            Temperature.removeModifiers(entity, trait, MealResistanceTempModifier.class);
        } else {
            Temperature.replaceOrAddModifier(entity, new MealResistanceTempModifier(amount), trait, Matcher.SAME_CLASS);
        }
    }
}
