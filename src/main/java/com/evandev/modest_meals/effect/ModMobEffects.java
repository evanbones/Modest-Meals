package com.evandev.modest_meals.effect;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.attribute.ModAttributes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
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

    public static final DeferredHolder<MobEffect, MobEffect> STAMINA_REGEN = MOB_EFFECTS.register(
            "stamina_regen",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x55FF55) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return false;
                }
            }
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

    public static final DeferredHolder<MobEffect, MobEffect> HEALTH_REGEN = MOB_EFFECTS.register(
            "health_regen",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF5555) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    int k = 40 >> amplifier;
                    if (k > 0) {
                        return duration % k == 0;
                    }
                    return true;
                }

                @Override
                public boolean applyEffectTick(LivingEntity entity, int amplifier) {
                    if (entity.getHealth() < entity.getMaxHealth()) {
                        entity.heal(1.0F);
                    }
                    return true;
                }
            }
    );

    public static final DeferredHolder<MobEffect, MobEffect> HEALTH_DEPLETION = MOB_EFFECTS.register(
            "health_depletion",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x4E004E) {
                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    int k = 40 >> amplifier;
                    if (k > 0) {
                        return duration % k == 0;
                    }
                    return true;
                }

                @Override
                public boolean applyEffectTick(LivingEntity entity, int amplifier) {
                    entity.hurt(entity.damageSources().magic(), 1.0F);
                    return true;
                }
            }
    );

    public static void register(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
    }
}
