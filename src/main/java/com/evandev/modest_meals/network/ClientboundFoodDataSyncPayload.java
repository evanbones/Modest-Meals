package com.evandev.modest_meals.network;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.FoodProfile;
import com.evandev.modest_meals.food.FoodProfileManager;
import com.evandev.modest_meals.food.ingredient.IngredientProfile;
import com.evandev.modest_meals.food.ingredient.IngredientProfileManager;
import com.evandev.modest_meals.food.ingredient.MealEffect;
import com.evandev.modest_meals.food.ingredient.MealEffectManager;
import com.evandev.modest_meals.food.meal.MealFormula;
import com.evandev.modest_meals.food.meal.MealFormulaManager;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitManager;
import com.evandev.modest_meals.trait.FoodTraitType;
import com.evandev.modest_meals.trait.TagTraitEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Map;

public record ClientboundFoodDataSyncPayload(
        Map<ResourceLocation, List<FoodTrait>> itemTraits,
        List<TagTraitEntry> tagTraits,
        Map<ResourceLocation, List<String>> suppressions,
        List<FoodProfile> profiles,
        Map<ResourceLocation, IngredientProfile> ingredients,
        List<IngredientProfileManager.TagEntry> ingredientTags,
        Map<ResourceLocation, MealEffect> mealEffects,
        Map<ResourceLocation, MealType> mealTypes,
        MealFormula formula
) implements CustomPacketPayload {
    public static final Type<ClientboundFoodDataSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "food_data_sync"));

    private static final Codec<ClientboundFoodDataSyncPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, FoodTraitType.CODEC.listOf()).fieldOf("item_traits")
                    .forGetter(ClientboundFoodDataSyncPayload::itemTraits),
            TagTraitEntry.CODEC.listOf().fieldOf("tag_traits")
                    .forGetter(ClientboundFoodDataSyncPayload::tagTraits),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.STRING.listOf()).fieldOf("suppressions")
                    .forGetter(ClientboundFoodDataSyncPayload::suppressions),
            FoodProfile.CODEC.listOf().fieldOf("profiles").forGetter(ClientboundFoodDataSyncPayload::profiles),
            Codec.unboundedMap(ResourceLocation.CODEC, IngredientProfile.CODEC).fieldOf("ingredients")
                    .forGetter(ClientboundFoodDataSyncPayload::ingredients),
            IngredientProfileManager.TagEntry.CODEC.listOf().fieldOf("ingredient_tags")
                    .forGetter(ClientboundFoodDataSyncPayload::ingredientTags),
            Codec.unboundedMap(ResourceLocation.CODEC, MealEffect.CODEC).fieldOf("meal_effects")
                    .forGetter(ClientboundFoodDataSyncPayload::mealEffects),
            Codec.unboundedMap(ResourceLocation.CODEC, MealType.CODEC).fieldOf("meal_types")
                    .forGetter(ClientboundFoodDataSyncPayload::mealTypes),
            MealFormula.CODEC.fieldOf("formula").forGetter(ClientboundFoodDataSyncPayload::formula)
    ).apply(instance, ClientboundFoodDataSyncPayload::new));

    public static final StreamCodec<ByteBuf, ClientboundFoodDataSyncPayload> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static ClientboundFoodDataSyncPayload create() {
        return new ClientboundFoodDataSyncPayload(
                FoodTraitManager.snapshotItemTraits(),
                FoodTraitManager.snapshotTagTraits(),
                FoodTraitManager.snapshotSuppressions(),
                FoodProfileManager.snapshotProfiles(),
                IngredientProfileManager.snapshotItemProfiles(),
                IngredientProfileManager.snapshotTagProfiles(),
                MealEffectManager.snapshot(),
                MealTypeManager.snapshot(),
                MealFormulaManager.get()
        );
    }

    public static void handle(ClientboundFoodDataSyncPayload payload, IPayloadContext context) {
        boolean updateBaseline = !context.connection().isMemoryConnection();
        context.enqueueWork(() -> {
            FoodTraitManager.applyFromNetwork(payload.itemTraits(), payload.tagTraits(), payload.suppressions(), updateBaseline);
            FoodProfileManager.applyFromNetwork(payload.profiles(), updateBaseline);
            IngredientProfileManager.applyFromNetwork(payload.ingredients(), payload.ingredientTags(), updateBaseline);
            MealEffectManager.applyFromNetwork(payload.mealEffects(), updateBaseline);
            MealTypeManager.applyFromNetwork(payload.mealTypes());
            MealFormulaManager.applyFromNetwork(payload.formula());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
