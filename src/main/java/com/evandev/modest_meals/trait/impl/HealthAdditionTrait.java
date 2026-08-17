package com.evandev.modest_meals.trait.impl;

import com.evandev.modest_meals.regen.HealthRegenHelper;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitType;
import com.evandev.modest_meals.trait.TraitTooltipHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record HealthAdditionTrait(float value, int duration) implements FoodTrait {
    public static final MapCodec<HealthAdditionTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("value").forGetter(HealthAdditionTrait::value),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(HealthAdditionTrait::duration)
    ).apply(instance, HealthAdditionTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HealthAdditionTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, HealthAdditionTrait::value,
            ByteBufCodecs.VAR_INT, HealthAdditionTrait::duration,
            HealthAdditionTrait::new
    );

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.HEALTH_ADDITION;
    }

    @Override
    public FoodTrait compoundWith(FoodTrait other, float valueMultiplier, float durationMultiplier) {
        if (other instanceof HealthAdditionTrait(float value1, int duration1)) {
            return new HealthAdditionTrait(
                    (this.value + value1) * valueMultiplier,
                    (int) ((this.duration + duration1) * durationMultiplier)
            );
        }
        return this;
    }

    @Override
    public void apply(LivingEntity entity, ItemStack stack, float valueMultiplier, float durationMultiplier) {
        float points = this.value * valueMultiplier;
        int digestTicks = (int) (this.duration * durationMultiplier);
        if (entity instanceof Player player) {
            HealthRegenHelper.get(player).addHealth(points, digestTicks, stack.getItem().hashCode());
        } else {
            entity.heal(points);
        }
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        return TraitTooltipHelper.formatPlusTrait("modest_meals.trait.health_addition",
                this.value * valueMultiplier, (long) (this.duration * durationMultiplier));
    }
}
