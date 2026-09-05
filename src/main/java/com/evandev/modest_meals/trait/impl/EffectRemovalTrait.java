package com.evandev.modest_meals.trait.impl;

import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitType;
import com.evandev.modest_meals.trait.TraitBenefit;
import com.evandev.modest_meals.trait.TraitTooltipHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record EffectRemovalTrait(boolean clearAll) implements FoodTrait {
    public static final MapCodec<EffectRemovalTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("clear_all", true).forGetter(EffectRemovalTrait::clearAll)
    ).apply(instance, EffectRemovalTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EffectRemovalTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, EffectRemovalTrait::clearAll,
            EffectRemovalTrait::new
    );

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.EFFECT_REMOVAL;
    }

    @Override
    public TraitBenefit benefit() {
        return TraitBenefit.UTILITY;
    }

    @Override
    public FoodTrait compoundWith(FoodTrait other, float valueMultiplier, float durationMultiplier) {
        if (other instanceof EffectRemovalTrait(boolean all)) {
            return new EffectRemovalTrait(this.clearAll || all);
        }
        return this;
    }

    @Override
    public void apply(LivingEntity entity, ItemStack stack, float valueMultiplier, float durationMultiplier) {
        if (this.clearAll) {
            entity.removeAllEffects();
        } else {
            List<MobEffectInstance> harmful = new ArrayList<>();
            for (MobEffectInstance inst : entity.getActiveEffects()) {
                if (inst.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                    harmful.add(inst);
                }
            }
            for (MobEffectInstance inst : harmful) {
                entity.removeEffect(inst.getEffect());
            }
        }
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        return TraitTooltipHelper.formatSimple(
                this.clearAll ? "modest_meals.trait.clear_all_effects" : "modest_meals.trait.clear_negative_effects",
                0
        );
    }
}
