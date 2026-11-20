package com.evandev.modest_meals.compat.cultural_delights;

//? if cultural_delights && <26.1 {
import com.baisylia.culturaldelights.recipes.ModRecipes;
import com.baisylia.culturaldelights.recipes.OvenShapedRecipe;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import com.evandev.modest_meals.recipe.AuthoredRecipes;
import com.evandev.modest_meals.recipe.MealGridMatcher;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
//?}
//? if neoforge_legacy
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
//? if cultural_delights && fabric && <26.1
//import vectorwing.farmersdelight.refabricated.inventory.RecipeWrapper;
//? if cultural_delights && <26.1 {

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OvenMealRecipe extends OvenShapedRecipe {

    private static final int GRID = 3;

    private final ResourceLocation mealTypeId;

    public OvenMealRecipe(ResourceLocation mealTypeId, int cookTime) {
        super(new ShapedRecipePattern(1, 1, NonNullList.withSize(1, Ingredient.EMPTY), Optional.empty()),
                ItemStack.EMPTY, cookTime);
        this.mealTypeId = mealTypeId;
    }

    public ResourceLocation mealTypeId() {
        return mealTypeId;
    }

    private static CraftingInput gridOf(RecipeWrapper inventory) {
        List<ItemStack> items = new ArrayList<>(GRID * GRID);
        for (int slot = 0; slot < GRID * GRID; slot++) {
            items.add(inventory.getItem(slot));
        }
        return CraftingInput.of(GRID, GRID, items);
    }

    private Optional<MealGridMatcher.Match> matchFor(RecipeWrapper inventory) {
        return MealGridMatcher.findMatch(MealTypeManager.oven(), gridOf(inventory))
                .filter(match -> match.type().id().equals(mealTypeId));
    }

    private Optional<MealType> mealType() {
        return MealTypeManager.get(mealTypeId)
                .filter(type -> type.station().effective() == MealType.Station.OVEN);
    }

    @Override
    public boolean matches(RecipeWrapper inventory, Level level) {
        if (mealType().isEmpty()) {
            return false;
        }
        Optional<ItemStack> result = matchFor(inventory).flatMap(MealGridMatcher.Match::assemble);
        return result.isPresent()
                && !AuthoredRecipes.anyMatch(level, ModRecipes.BAKING_SHAPED.get(), inventory, OvenMealRecipe.class)
                && !AuthoredRecipes.anyMatch(level, ModRecipes.BAKING.get(), inventory, OvenMealRecipe.class);
    }

    @Override
    public ItemStack assemble(RecipeWrapper inventory, HolderLookup.Provider registries) {
        return matchFor(inventory).flatMap(MealGridMatcher.Match::assemble).orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return mealType().flatMap(type -> type.resolveItem().map(item -> new ItemStack(item, type.servings())))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= GRID && height >= GRID;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CulturalDelightsCompat.OVEN_MEAL_SERIALIZER.get();
    }

    public static RecipeSerializer<OvenMealRecipe> serializer() {
        return new Serializer();
    }

    public static class Serializer implements RecipeSerializer<OvenMealRecipe> {
        private static final MapCodec<OvenMealRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("meal_type").forGetter(OvenMealRecipe::mealTypeId),
                Codec.INT.optionalFieldOf("cooktime", 200).forGetter(OvenMealRecipe::getCookTime)
        ).apply(instance, OvenMealRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, OvenMealRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, OvenMealRecipe::mealTypeId,
                        ByteBufCodecs.VAR_INT, OvenMealRecipe::getCookTime,
                        OvenMealRecipe::new
                );

        @Override
        public MapCodec<OvenMealRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, OvenMealRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
//?}
