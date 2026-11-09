package com.evandev.modest_meals.compat.farmers_delight;

//? if farmers_delight {
import com.evandev.modest_meals.food.ingredient.IngredientProfileManager;
import com.evandev.modest_meals.food.meal.MealAssembler;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import com.evandev.modest_meals.recipe.AuthoredRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
//? if >=26.1 {
/*import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import vectorwing.farmersdelight.common.crafting.CookingPotBookCategory;
*///?} else {
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
//?}
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
//? if >=26.1
/*import net.minecraft.world.item.Items;*/
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
//? if neoforge {
/*import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
*///?} else
import vectorwing.farmersdelight.refabricated.inventory.RecipeWrapper;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MealCookingRecipe extends CookingPotRecipe {

    //? if >=26.1
    /*private static final List<Ingredient> PROPERTY_SET_PLACEHOLDER_INPUT = List.of(Ingredient.of(Items.BOWL));*/

    private final ResourceLocation mealTypeId;
    private final int cookTime;

    public MealCookingRecipe(ResourceLocation mealTypeId, int cookTime) {
        //? if >=26.1 {
        /*super("", CookingPotBookCategory.MEALS, PROPERTY_SET_PLACEHOLDER_INPUT, new ItemStackTemplate(Items.BOWL),
                Optional.empty(), 0.0F, cookTime);
        *///?} else
        super("", null, NonNullList.create(), ItemStack.EMPTY, ItemStack.EMPTY, 0.0F, cookTime);
        this.mealTypeId = mealTypeId;
        this.cookTime = cookTime;
    }

    private static List<ItemStack> inputsOf(RecipeWrapper inventory) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int slot = 0; slot < INPUT_SLOTS; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        return inputs;
    }

    public ResourceLocation mealTypeId() {
        return mealTypeId;
    }

    private Optional<MealType> mealType() {
        return MealTypeManager.get(mealTypeId)
                .filter(type -> type.station() == MealType.Station.COOKING_POT);
    }

    private Optional<MealType.Split> ingredientsFor(RecipeWrapper inventory, MealType type) {
        Optional<MealType.Split> split = type.splitBase(inputsOf(inventory));
        if (split.isEmpty()) {
            return Optional.empty();
        }

        List<ItemStack> ingredients = split.get().ingredients();
        if (ingredients.isEmpty() || !type.accepts(ingredients)) {
            return Optional.empty();
        }
        return ingredients.stream().allMatch(IngredientProfileManager::isIngredient)
                ? split
                : Optional.empty();
    }

    private boolean isBestMatch(RecipeWrapper inventory, MealType mine) {
        for (MealType other : MealTypeManager.cookingPot()) {
            if (other.id().equals(mine.id())) {
                return true;
            }
            if (ingredientsFor(inventory, other).isPresent()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matches(RecipeWrapper inventory, Level level) {
        Optional<MealType> mine = mealType();
        return mine.isPresent()
                && ingredientsFor(inventory, mine.get()).isPresent()
                && isBestMatch(inventory, mine.get())
                && !AuthoredRecipes.anyMatch(level, ModRecipeTypes.COOKING.get(), inventory, MealCookingRecipe.class);
    }

    //? if >=26.1 {
    /*@Override
    public ItemStack assemble(RecipeWrapper inventory) {
        return assembleMeal(inventory);
    }

    @Override
    public ItemStackTemplate container() {
        return mealType()
                .flatMap(MealType::resolveContainer)
                .map(ItemStackTemplate::new)
                .orElse(null);
    }

    @Override
    public ItemStackTemplate result() {
        return mealType()
                .flatMap(type -> type.resolveItem().map(item -> new ItemStackTemplate(item, type.servings())))
                .orElseGet(super::result);
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Override
    public RecipeSerializer<MealCookingRecipe> getSerializer() {
        return FarmersDelightCompat.MEAL_COOKING_SERIALIZER.get();
    }

    public static RecipeSerializer<MealCookingRecipe> serializer() {
        return new RecipeSerializer<>(Serializer.CODEC, Serializer.STREAM_CODEC);
    }
    *///?} else {
    @Override
    public ItemStack assemble(RecipeWrapper inventory, HolderLookup.Provider registries) {
        return assembleMeal(inventory);
    }

    @Override
    public ItemStack getOutputContainer() {
        return mealType()
                .flatMap(MealType::resolveContainer)
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return mealType().flatMap(type -> type.resolveItem().map(item -> new ItemStack(item, type.servings())))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FarmersDelightCompat.MEAL_COOKING_SERIALIZER.get();
    }

    public static RecipeSerializer<MealCookingRecipe> serializer() {
        return new Serializer();
    }
    //?}

    private ItemStack assembleMeal(RecipeWrapper inventory) {
        return mealType()
                .flatMap(type -> ingredientsFor(inventory, type)
                        .flatMap(split -> MealAssembler.assemble(type, split.ingredients(), split.base())))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public int getCookTime() {
        return cookTime;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    //? if >=26.1 {
    /*private static class Serializer {
    *///?} else
    public static class Serializer implements RecipeSerializer<MealCookingRecipe> {
        private static final MapCodec<MealCookingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("meal_type").forGetter(MealCookingRecipe::mealTypeId),
                Codec.INT.optionalFieldOf("cookingtime", 200)
                        .forGetter(MealCookingRecipe::getCookTime)
        ).apply(instance, MealCookingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, MealCookingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, MealCookingRecipe::mealTypeId,
                        ByteBufCodecs.VAR_INT, MealCookingRecipe::getCookTime,
                        MealCookingRecipe::new
                );

        //? if <26.1 {
        @Override
        public MapCodec<MealCookingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MealCookingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
        //?}
    }
}
//?}
