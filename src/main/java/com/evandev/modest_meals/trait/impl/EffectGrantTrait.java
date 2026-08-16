package com.evandev.modest_meals.trait.impl;

import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitType;
import com.evandev.modest_meals.trait.TraitTooltipHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public record EffectGrantTrait(Holder<MobEffect> effect, int duration, int amplifier, boolean showParticles, boolean ambient) implements FoodTrait {
    public static final MapCodec<EffectGrantTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(EffectGrantTrait::effect),
            Codec.INT.fieldOf("duration").forGetter(EffectGrantTrait::duration),
            Codec.INT.optionalFieldOf("amplifier", 0).forGetter(EffectGrantTrait::amplifier),
            Codec.BOOL.optionalFieldOf("show_particles", true).forGetter(EffectGrantTrait::showParticles),
            Codec.BOOL.optionalFieldOf("ambient", false).forGetter(EffectGrantTrait::ambient)
    ).apply(instance, EffectGrantTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EffectGrantTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.MOB_EFFECT), EffectGrantTrait::effect,
            ByteBufCodecs.VAR_INT, EffectGrantTrait::duration,
            ByteBufCodecs.VAR_INT, EffectGrantTrait::amplifier,
            ByteBufCodecs.BOOL, EffectGrantTrait::showParticles,
            ByteBufCodecs.BOOL, EffectGrantTrait::ambient,
            EffectGrantTrait::new
    );

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.EFFECT_GRANT;
    }

    @Override
    public FoodTrait compoundWith(FoodTrait other, float valueMultiplier, float durationMultiplier) {
        if (other instanceof EffectGrantTrait o && this.effect.equals(o.effect)) {
            int newAmp = Math.max(this.amplifier, o.amplifier);
            int newDur = (int) ((this.duration + o.duration) * durationMultiplier);
            return new EffectGrantTrait(this.effect, newDur, newAmp, this.showParticles || o.showParticles, this.ambient && o.ambient);
        }
        return this;
    }

    @Override
    public void apply(LivingEntity entity, float valueMultiplier, float durationMultiplier) {
        int dur = (int) (this.duration * durationMultiplier);
        if (dur > 0) {
            entity.addEffect(new MobEffectInstance(this.effect, dur, this.amplifier, this.ambient, this.showParticles));
        }
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        boolean isHarmful = this.effect.value().getCategory() == MobEffectCategory.HARMFUL;
        ChatFormatting color = isHarmful ? ChatFormatting.RED : ChatFormatting.BLUE;

        MutableComponent effectName = Component.translatable(this.effect.value().getDescriptionId());
        if (this.amplifier > 0) {
            effectName = Component.translatable("potion.withAmplifier", effectName, Component.translatable("potion.potency." + this.amplifier));
        }

        MutableComponent line = Component.translatable(isHarmful ? "modest_meals.trait.simple.take" : "modest_meals.trait.simple.plus", effectName.withStyle(color))
                .withStyle(color);

        long dur = (long) (this.duration * durationMultiplier);
        if (dur > 0) {
            line.append(Component.literal(" (" + TraitTooltipHelper.formatDuration(dur) + ")").withStyle(ChatFormatting.GRAY));
        }
        return line;
    }
}
