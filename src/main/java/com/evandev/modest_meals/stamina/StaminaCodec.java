package com.evandev.modest_meals.stamina;

import com.evandev.modest_meals.Constants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.player.Player;

public final class StaminaCodec {
    public static final String NBT_KEY = Constants.MOD_ID + "_stamina";

    private StaminaCodec() {
    }

    public static Codec<StaminaData> create() {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("stamina").forGetter(StaminaData::getStamina),
                Codec.INT.fieldOf("remaining").forGetter(StaminaData::getRemaining),
                Codec.INT.fieldOf("cooldown").forGetter(StaminaData::getCooldown),
                Codec.BOOL.fieldOf("exhausted").forGetter(StaminaData::isExhausted)
        ).apply(instance, StaminaData::new));
    }

    public static void save(Player player, CompoundTag compound) {
        if (player instanceof StaminaHolder holder) {
            StaminaData.CODEC.encodeStart(NbtOps.INSTANCE, holder.mm$getStaminaData())
                    .resultOrPartial(error -> Constants.LOG.error("Failed to serialize player stamina: {}", error))
                    .ifPresent(tag -> compound.put(NBT_KEY, tag));
        }
    }

    public static void read(Player player, CompoundTag compound) {
        if (player instanceof StaminaHolder holder && compound.contains(NBT_KEY)) {
            StaminaData.CODEC.parse(NbtOps.INSTANCE, compound.get(NBT_KEY))
                    .resultOrPartial(error -> Constants.LOG.error("Failed to parse player stamina: {}", error))
                    .ifPresent(holder::mm$setStaminaData);
        }
    }
}
