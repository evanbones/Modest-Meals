package com.evandev.modest_meals.trait.impl;

import com.evandev.modest_meals.network.ClientboundStaminaSyncPayload;
import com.evandev.modest_meals.network.ModNetworking;
import com.evandev.modest_meals.stamina.StaminaHelper;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record StaminaAdditionTrait(float value) implements FoodTrait {
    public static final MapCodec<StaminaAdditionTrait> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("value").forGetter(StaminaAdditionTrait::value)
    ).apply(instance, StaminaAdditionTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StaminaAdditionTrait> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, StaminaAdditionTrait::value,
            StaminaAdditionTrait::new
    );

    @Override
    public FoodTraitType<?> getType() {
        return FoodTraitType.STAMINA_ADDITION;
    }

    @Override
    public FoodTrait compoundWith(FoodTrait other, float valueMultiplier, float durationMultiplier) {
        if (other instanceof StaminaAdditionTrait(float value1)) {
            return new StaminaAdditionTrait((this.value + value1) * valueMultiplier);
        }
        return this;
    }

    @Override
    public void apply(LivingEntity entity, ItemStack stack, float valueMultiplier, float durationMultiplier) {
        if (entity instanceof Player player) {
            StaminaHelper.get(player).addLevels(this.value * valueMultiplier);

            if (player instanceof ServerPlayer serverPlayer) {
                ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
            }
        }
    }

    @Override
    public Component getTooltipComponent(double valueMultiplier, double durationMultiplier) {
        return TraitTooltipHelper.formatPlusTrait("modest_meals.trait.stamina_addition",
                this.value * valueMultiplier, 0);
    }
}
