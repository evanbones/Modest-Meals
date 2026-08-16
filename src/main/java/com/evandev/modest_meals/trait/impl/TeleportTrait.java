package com.evandev.modest_meals.trait.impl;

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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public record TeleportTrait(float range) implements FoodTrait {
    public static final MapCodec<TeleportTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("range", 16.0f).forGetter(TeleportTrait::range)
    ).apply(instance, TeleportTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TeleportTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, TeleportTrait::range,
            TeleportTrait::new
    );

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.TELEPORT;
    }

    @Override
    public FoodTrait compoundWith(FoodTrait other, float valueMultiplier, float durationMultiplier) {
        if (other instanceof TeleportTrait(float range1)) {
            return new TeleportTrait(Math.max(this.range, range1) * valueMultiplier);
        }
        return this;
    }

    @Override
    public void apply(LivingEntity entity, float valueMultiplier, float durationMultiplier) {
        if (entity.level().isClientSide()) return;
        float actualRange = this.range * valueMultiplier;
        for (int i = 0; i < 16; i++) {
            double d0 = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * actualRange;
            double d1 = Mth.clamp(entity.getY() + (entity.getRandom().nextInt((int) Math.max(1, actualRange)) - (actualRange / 2.0)),
                    entity.level().getMinBuildHeight(),
                    entity.level().getMinBuildHeight() + ((ServerLevel) entity.level()).getLogicalHeight() - 1);
            double d2 = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * actualRange;

            if (entity.isPassenger()) {
                entity.stopRiding();
            }

            Vec3 initialPos = entity.position();
            if (entity.randomTeleport(d0, d1, d2, true)) {
                entity.level().gameEvent(GameEvent.TELEPORT, initialPos, GameEvent.Context.of(entity));
                entity.level().playSound(null, initialPos.x, initialPos.y, initialPos.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                entity.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                break;
            }
        }
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        return TraitTooltipHelper.formatSimplePlus("modest_meals.trait.teleport_randomly", 0);
    }
}
