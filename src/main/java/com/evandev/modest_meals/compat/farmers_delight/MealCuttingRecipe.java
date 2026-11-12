package com.evandev.modest_meals.compat.farmers_delight;

//? if farmers_delight {
import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.tag.ModItemTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
//? if >=26.1 {
/*import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStackTemplate;
*///?} else {
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
//?}
//? if neoforge {
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
//?} else
//import vectorwing.farmersdelight.refabricated.inventory.RecipeWrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.ingredient.ChanceResult;

import java.util.List;
//? if <26.1
import java.util.Optional;

public class MealCuttingRecipe extends CuttingBoardRecipe {

    private final Item whole;
    private final Item portion;
    private final int count;

    //? if >=26.1 {
    /*public MealCuttingRecipe(Item whole, Item portion, int count, Ingredient tool) {
        super("", Ingredient.of(whole), tool,
                List.of(new ChanceResult(new ItemStackTemplate(portion, count), 1.0F)),
                (Holder<SoundEvent>) null);
        this.whole = whole;
        this.portion = portion;
        this.count = count;
    }
    *///?} else {
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
        return cut(inventory.getItem(0));
    }
    //?}

    public List<ItemStack> cut(ItemStack meal) {
        MealContents contents = meal.get(ModDataComponents.MEAL_CONTENTS.get());
        ItemStack portions = new ItemStack(portion, count);
        if (contents != null) {
            portions.set(ModDataComponents.MEAL_CONTENTS.get(), contents.split(count));
        }
        return List.of(portions);
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

    //? if >=26.1 {
    /*@Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<CuttingBoardRecipe> getSerializer() {
        return (RecipeSerializer) FarmersDelightCompat.MEAL_CUTTING_SERIALIZER.get();
    }

    private static final MapCodec<MealCuttingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryOps.retrieveGetter(Registries.ITEM),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("meal").forGetter(MealCuttingRecipe::whole),
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("portion").forGetter(MealCuttingRecipe::portion),
            Codec.intRange(1, 64).fieldOf("count").forGetter(MealCuttingRecipe::count)
    ).apply(instance, (items, whole, portion, count) ->
            new MealCuttingRecipe(whole, portion, count, Ingredient.of(items.getOrThrow(ModItemTags.KNIVES)))));

    private static final StreamCodec<RegistryFriendlyByteBuf, MealCuttingRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.registry(Registries.ITEM), MealCuttingRecipe::whole,
                    ByteBufCodecs.registry(Registries.ITEM), MealCuttingRecipe::portion,
                    ByteBufCodecs.VAR_INT, MealCuttingRecipe::count,
                    Ingredient.CONTENTS_STREAM_CODEC, MealCuttingRecipe::getTool,
                    MealCuttingRecipe::new
            );

    public static RecipeSerializer<MealCuttingRecipe> serializer() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
    *///?} else {
    @Override
    public RecipeSerializer<?> getSerializer() {
        return FarmersDelightCompat.MEAL_CUTTING_SERIALIZER.get();
    }

    public static RecipeSerializer<MealCuttingRecipe> serializer() {
        return new Serializer();
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
    //?}
}
//?}
