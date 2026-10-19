package com.evandev.modest_meals.compat.cultural_delights;

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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lets one {@link MealType} be made in Cultural Delights' vat.
 */
public class VatMealRecipe extends VatRecipe {

    private static final int INPUT_SLOTS = 6;
    private static final int CONTAINER_SLOT = 6;
    private static final int OUTPUT_SLOT = 7;

    private final ResourceLocation mealTypeId;

    public VatMealRecipe(ResourceLocation mealTypeId, VatTemperature temperature, int cookTime) {
        super(ItemStack.EMPTY, NonNullList.create(), temperature, Ingredient.EMPTY, cookTime);
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

    @Override
    public ItemStack assemble(RecipeWrapper inventory, HolderLookup.Provider registries) {
        return mealType()
                .flatMap(type -> ingredientsFor(inventory, type)
                        .flatMap(split -> MealAssembler.assemble(type, split.ingredients(), split.base())))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return mealType().flatMap(MealType::resolveItem)
                .map(item -> new ItemStack(item, mealType().get().servings()))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getResultItem() {
        return getResultItem(null);
    }

    @Override
    public Ingredient getContainer() {
        return mealType().flatMap(MealType::resolveContainer).map(Ingredient::of).orElse(Ingredient.EMPTY);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CulturalDelightsCompat.VAT_MEAL_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<VatMealRecipe> {
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

        @Override
        public MapCodec<VatMealRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, VatMealRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
