package com.evandev.modest_meals.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * Everything a cooked meal does, worked out from its ingredients at craft time and carried on the stack.
 */
public record MealContents(
        float health,
        float stamina,
        int digestTicks,
        float temporaryHealth,
        Optional<ResourceLocation> effect,
        int amplifier,
        int durationTicks
) {
    public static final MealContents EMPTY = new MealContents(
            0.0F, 0.0F, 0, 0.0F, Optional.empty(), 0, 0);

    public static final Codec<MealContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("health", 0.0F).forGetter(MealContents::health),
            Codec.FLOAT.optionalFieldOf("stamina", 0.0F).forGetter(MealContents::stamina),
            Codec.INT.optionalFieldOf("digest_ticks", 0).forGetter(MealContents::digestTicks),
            Codec.FLOAT.optionalFieldOf("temporary_health", 0.0F).forGetter(MealContents::temporaryHealth),
            ResourceLocation.CODEC.optionalFieldOf("effect").forGetter(MealContents::effect),
            Codec.INT.optionalFieldOf("amplifier", 0).forGetter(MealContents::amplifier),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(MealContents::durationTicks)
    ).apply(instance, MealContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MealContents> STREAM_CODEC =
            ByteBufCodecs.<MealContents>fromCodec(CODEC).cast();

    /**
     * Whether this meal grants an effect at all.
     */
    public boolean hasEffect() {
        return effect.isPresent() && durationTicks > 0;
    }

    /**
     * Whether this meal restores or grants anything. A meal that does nothing should not have been cooked.
     */
    public boolean isMeaningful() {
        return health > 0.0F || stamina > 0.0F
                || temporaryHealth > 0.0F
                || hasEffect();
    }
}
