package com.evandev.modest_meals.effect;

import com.evandev.modest_meals.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Constants.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> NOURISHMENT_HEALTH_BOOST = MOB_EFFECTS.register(
            "nourishment_health_boost",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x98D982) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return false;
                }
            }.addAttributeModifier(
                    Attributes.MAX_HEALTH,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "effect.nourishment_health_boost"),
                    2.0,
                    AttributeModifier.Operation.ADD_VALUE
            )
    );

    public static void register(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
    }
}
