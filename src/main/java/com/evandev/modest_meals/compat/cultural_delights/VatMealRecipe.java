package com.evandev.modest_meals.compat.cultural_delights;

//? if cultural_delights {
import com.baisylia.culturaldelights.recipes.ModRecipes;
import com.baisylia.culturaldelights.recipes.VatRecipe;
import com.baisylia.culturaldelights.util.VatTemperature;
import com.evandev.modest_meals.food.ingredient.IngredientProfileManager;
import com.evandev.modest_meals.food.meal.MealAssembler;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import com.evandev.modest_meals.recipe.AuthoredRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
//?}
//? if cultural_delights && <26.1
import net.minecraft.core.HolderLookup;
//? if neoforge_legacy
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
//? if cultural_delights && fabric
//import vectorwing.farmersdelight.refabricated.inventory.RecipeWrapper;
//? if cultural_delights && >=26.1 {
/*import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.PlacementInfo;
*///?}
//? if cultural_delights {

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VatMealRecipe extends VatRecipe {

    private static final int INPUT_SLOTS = 6;
    private static final int CONTAINER_SLOT = 6;
    private static final int OUTPUT_SLOT = 7;

    private final ResourceLocation mealTypeId;

    public VatMealRecipe(ResourceLocation mealTypeId, VatTemperature temperature, int cookTime) {
//?}
        //? if cultural_delights && <26.1
        super(ItemStack.EMPTY, NonNullList.create(), temperature, Ingredient.EMPTY, cookTime);
        //? if cultural_delights && >=26.1
        /*super(new ItemStackTemplate(Items.BOWL), NonNullList.create(), temperature, Optional.empty(), cookTime);*/
//? if cultural_delights {
        this.mealTypeId = mealTypeId;
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

    private static boolean containerMatches(RecipeWrapper inventory, MealType type) {
        ItemStack held = inventory.getItem(CONTAINER_SLOT);
        return type.resolveContainer().map(held::is).orElse(held.isEmpty());
    }

    public ResourceLocation mealTypeId() {
        return mealTypeId;
    }

    private Optional<MealType> mealType() {
        return MealTypeManager.get(mealTypeId)
                .filter(type -> type.station() == MealType.Station.VAT);
    }

    private static Optional<MealType.Split> ingredientsFor(RecipeWrapper inventory, MealType type) {
        if (!containerMatches(inventory, type)) {
            return Optional.empty();
        }

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

    private static boolean isBestMatch(RecipeWrapper inventory, MealType mine) {
        for (MealType other : MealTypeManager.vat()) {
            if (other.id().equals(mine.id())) {
                return true;
            }
            if (ingredientsFor(inventory, other).isPresent()) {
                return false;
            }
        }
        return true;
    }

    private static boolean fitsOutput(RecipeWrapper inventory, ItemStack result) {
        ItemStack held = inventory.getItem(OUTPUT_SLOT);
        if (held.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(held, result)
                && held.getCount() + result.getCount() <= held.getMaxStackSize();
    }

    @Override
    public boolean matches(RecipeWrapper inventory, Level level) {
        Optional<MealType> mine = mealType();
        if (mine.isEmpty()) {
            return false;
        }
        Optional<MealType.Split> split = ingredientsFor(inventory, mine.get());
        if (split.isEmpty() || !isBestMatch(inventory, mine.get())
                || AuthoredRecipes.anyMatch(level, ModRecipes.AGING_TYPE.get(), inventory, VatMealRecipe.class)) {
            return false;
        }
        return MealAssembler.assemble(mine.get(), split.get().ingredients(), split.get().base())
                .filter(result -> fitsOutput(inventory, result))
                .isPresent();
    }

    private ItemStack assembleMeal(RecipeWrapper inventory) {
        return mealType()
                .flatMap(type -> ingredientsFor(inventory, type)
                        .flatMap(split -> MealAssembler.assemble(type, split.ingredients(), split.base())))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getResultItem() {
        return mealType().flatMap(type -> type.resolveItem().map(item -> new ItemStack(item, type.servings())))
                .orElse(ItemStack.EMPTY);
    }

    private Optional<Ingredient> containerIngredient() {
        return mealType().flatMap(MealType::resolveContainer).map(Ingredient::of);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
//?}
    //? if cultural_delights && <26.1 {
    @Override
    public ItemStack assemble(RecipeWrapper inventory, HolderLookup.Provider registries) {
        return assembleMeal(inventory);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return getResultItem();
    }

    @Override
    public Ingredient getContainer() {
        return containerIngredient().orElse(Ingredient.EMPTY);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CulturalDelightsCompat.VAT_MEAL_SERIALIZER.get();
    }

    public static RecipeSerializer<VatMealRecipe> serializer() {
        return new Serializer();
    }

    public static class Serializer implements RecipeSerializer<VatMealRecipe> {
    //?}
    //? if cultural_delights && >=26.1 {
    /*@Override
    public ItemStack assemble(RecipeWrapper inventory) {
        return assembleMeal(inventory);
    }

    @Override
    public ItemStackTemplate getOutput() {
        return mealType()
                .flatMap(type -> type.resolveItem().map(item -> new ItemStackTemplate(item, type.servings())))
                .orElseGet(super::getOutput);
    }

    @Override
    public Optional<Ingredient> getContainer() {
        return containerIngredient();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeSerializer<VatMealRecipe> getSerializer() {
        return CulturalDelightsCompat.VAT_MEAL_SERIALIZER.get();
    }

    public static RecipeSerializer<VatMealRecipe> serializer() {
        return new RecipeSerializer<>(Serializer.CODEC, Serializer.STREAM_CODEC);
    }

    private static class Serializer {
    *///?}
    //? if cultural_delights {
        private static final MapCodec<VatMealRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("meal_type").forGetter(VatMealRecipe::mealTypeId),
                VatTemperature.CODEC.optionalFieldOf("temperature", VatTemperature.COLD)
                        .forGetter(VatMealRecipe::getTemperature),
                Codec.INT.optionalFieldOf("cooktime", 200).forGetter(VatMealRecipe::getCookTime)
        ).apply(instance, VatMealRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, VatMealRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC, VatMealRecipe::mealTypeId,
                        ByteBufCodecs.fromCodec(VatTemperature.CODEC), VatMealRecipe::getTemperature,
                        ByteBufCodecs.VAR_INT, VatMealRecipe::getCookTime,
                        VatMealRecipe::new
                );
    //?}
        //? if cultural_delights && <26.1 {
        @Override
        public MapCodec<VatMealRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, VatMealRecipe> streamCodec() {
            return STREAM_CODEC;
        }
        //?}
//? if cultural_delights {
    }
}
//?}
