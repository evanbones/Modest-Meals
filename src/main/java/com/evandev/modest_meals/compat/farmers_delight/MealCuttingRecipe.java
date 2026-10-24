package com.evandev.modest_meals.compat.farmers_delight;

import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.tag.ModItemTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.ingredient.ChanceResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cuts a whole meal into portions on Farmer's Delight's cutting board, each portion keeping its share of the meal.
 */
public class MealCuttingRecipe extends CuttingBoardRecipe {

    private final Item whole;
    private final Item portion;
    private final int count;

    public MealCuttingRecipe(Item whole, Item portion, int count) {
        super("", Ingredient.of(whole), Ingredient.of(ModItemTags.KNIVES),
                NonNullList.of(ChanceResult.EMPTY, new ChanceResult(new ItemStack(portion, count), 1.0F)),
                Optional.empty());
        this.whole = whole;
        this.portion = portion;
        this.count = count;
    }

    @Override
    public List<ItemStack> rollResults(RandomSource random, int fortuneLevel, RecipeWrapper inventory) {
        MealContents contents = inventory.getItem(0).get(ModDataComponents.MEAL_CONTENTS.get());
        List<ItemStack> results = new ArrayList<>();
        ItemStack portions = new ItemStack(portion, count);
        if (contents != null) {
            portions.set(ModDataComponents.MEAL_CONTENTS.get(), contents.split(count));
        }
        results.add(portions);
        return results;
    }

    public Item whole() {
        return whole;
    }

    public Item portion() {
        return portion;
    }

    public int count() {
        return count;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FarmersDelightCompat.MEAL_CUTTING_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<MealCuttingRecipe> {
        private static final MapCodec<MealCuttingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("meal").forGetter(MealCuttingRecipe::whole),
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("portion").forGetter(MealCuttingRecipe::portion),
                Codec.intRange(1, 64).fieldOf("count").forGetter(MealCuttingRecipe::count)
        ).apply(instance, MealCuttingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, MealCuttingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.registry(Registries.ITEM), MealCuttingRecipe::whole,
                        ByteBufCodecs.registry(Registries.ITEM), MealCuttingRecipe::portion,
                        ByteBufCodecs.VAR_INT, MealCuttingRecipe::count,
                        MealCuttingRecipe::new
                );

        @Override
        public MapCodec<MealCuttingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MealCuttingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
