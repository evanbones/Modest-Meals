package com.evandev.modest_meals.network;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.FoodProfile;
import com.evandev.modest_meals.food.FoodProfileManager;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitManager;
import com.evandev.modest_meals.trait.FoodTraitType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Map;

public record ClientboundFoodDataSyncPayload(
        Map<ResourceLocation, List<FoodTrait>> itemTraits,
        Map<TagKey<Item>, List<FoodTrait>> tagTraits,
        Map<ResourceLocation, List<String>> suppressions,
        List<FoodProfile> profiles
) implements CustomPacketPayload {
    public static final Type<ClientboundFoodDataSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "food_data_sync"));

    private static final Codec<ClientboundFoodDataSyncPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, FoodTraitType.CODEC.listOf()).fieldOf("item_traits")
                    .forGetter(ClientboundFoodDataSyncPayload::itemTraits),
            Codec.unboundedMap(TagKey.codec(Registries.ITEM), FoodTraitType.CODEC.listOf()).fieldOf("tag_traits")
                    .forGetter(ClientboundFoodDataSyncPayload::tagTraits),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.STRING.listOf()).fieldOf("suppressions")
                    .forGetter(ClientboundFoodDataSyncPayload::suppressions),
            FoodProfile.CODEC.listOf().fieldOf("profiles").forGetter(ClientboundFoodDataSyncPayload::profiles)
    ).apply(instance, ClientboundFoodDataSyncPayload::new));

    public static final StreamCodec<ByteBuf, ClientboundFoodDataSyncPayload> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static ClientboundFoodDataSyncPayload create() {
        return new ClientboundFoodDataSyncPayload(
                FoodTraitManager.snapshotItemTraits(),
                FoodTraitManager.snapshotTagTraits(),
                FoodTraitManager.snapshotSuppressions(),
                FoodProfileManager.snapshotProfiles()
        );
    }

    public static void handle(ClientboundFoodDataSyncPayload payload, IPayloadContext context) {
        boolean updateBaseline = !context.connection().isMemoryConnection();
        context.enqueueWork(() -> {
            FoodTraitManager.applyFromNetwork(payload.itemTraits(), payload.tagTraits(), payload.suppressions(), updateBaseline);
            FoodProfileManager.applyFromNetwork(payload.profiles(), updateBaseline);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
