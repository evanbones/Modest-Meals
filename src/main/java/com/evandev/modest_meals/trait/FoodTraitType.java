package com.evandev.modest_meals.trait;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.trait.impl.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public record FoodTraitType<T extends FoodTrait>(MapCodec<T> codec,
                                                 StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
    public static final ResourceKey<Registry<FoodTraitType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "food_trait_types")
    );

    public static final DeferredRegister<FoodTraitType<?>> TRAIT_TYPES = DeferredRegister.create(REGISTRY_KEY, Constants.MOD_ID);
    public static final FoodTraitType<StaminaRegenTrait> STAMINA_REGEN = register("stamina_regen", StaminaRegenTrait.MAP_CODEC, StaminaRegenTrait.STREAM_CODEC);
    public static final FoodTraitType<StaminaAdditionTrait> STAMINA_ADDITION = register("stamina_addition", StaminaAdditionTrait.MAP_CODEC, StaminaAdditionTrait.STREAM_CODEC);
    public static final FoodTraitType<StaminaDepletionTrait> STAMINA_DEPLETION = register("stamina_depletion", StaminaDepletionTrait.MAP_CODEC, StaminaDepletionTrait.STREAM_CODEC);
    public static final FoodTraitType<StaminaNoRegenTrait> STAMINA_NO_REGEN = register("stamina_no_regen", StaminaNoRegenTrait.MAP_CODEC, StaminaNoRegenTrait.STREAM_CODEC);
    public static final FoodTraitType<StaminaCapacityTrait> STAMINA_CAPACITY = register("stamina_capacity", StaminaCapacityTrait.MAP_CODEC, StaminaCapacityTrait.STREAM_CODEC);
    public static final FoodTraitType<HealthRegenTrait> HEALTH_REGEN = register("health_regen", HealthRegenTrait.MAP_CODEC, HealthRegenTrait.STREAM_CODEC);
    public static final FoodTraitType<HealthAdditionTrait> HEALTH_ADDITION = register("health_addition", HealthAdditionTrait.MAP_CODEC, HealthAdditionTrait.STREAM_CODEC);
    public static final FoodTraitType<HealthDepletionTrait> HEALTH_DEPLETION = register("health_depletion", HealthDepletionTrait.MAP_CODEC, HealthDepletionTrait.STREAM_CODEC);
    public static final FoodTraitType<HealthNoRegenTrait> HEALTH_NO_REGEN = register("health_no_regen", HealthNoRegenTrait.MAP_CODEC, HealthNoRegenTrait.STREAM_CODEC);
    public static final FoodTraitType<EffectGrantTrait> EFFECT_GRANT = register("effect_grant", EffectGrantTrait.MAP_CODEC, EffectGrantTrait.STREAM_CODEC);
    public static final FoodTraitType<EffectRemovalTrait> EFFECT_REMOVAL = register("effect_removal", EffectRemovalTrait.MAP_CODEC, EffectRemovalTrait.STREAM_CODEC);
    public static final FoodTraitType<TeleportTrait> TELEPORT = register("teleport_randomly", TeleportTrait.MAP_CODEC, TeleportTrait.STREAM_CODEC);
    public static final FoodTraitType<AirBubblesTrait> AIR_BUBBLES = register("air_bubbles", AirBubblesTrait.MAP_CODEC, AirBubblesTrait.STREAM_CODEC);
    public static final FoodTraitType<FireExtinguishTrait> FIRE_EXTINGUISH = register("fire_extinguish", FireExtinguishTrait.MAP_CODEC, FireExtinguishTrait.STREAM_CODEC);
    public static final Registry<FoodTraitType<?>> REGISTRY = TRAIT_TYPES.makeRegistry(builder -> builder.sync(true));
    public static final Codec<FoodTrait> CODEC = Codec.lazyInitialized(() ->
            REGISTRY.byNameCodec().dispatch("type", FoodTrait::getType, FoodTraitType::codec)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FoodTrait> STREAM_CODEC = ByteBufCodecs.registry(REGISTRY_KEY)
            .dispatch(FoodTrait::getType, FoodTraitType::streamCodec);

    private static <T extends FoodTrait> FoodTraitType<T> register(String name, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        FoodTraitType<T> type = new FoodTraitType<>(codec, streamCodec);
        TRAIT_TYPES.register(name, () -> type);
        return type;
    }

    public static void register(IEventBus modBus) {
        TRAIT_TYPES.register(modBus);
    }
}
