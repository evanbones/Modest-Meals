package com.evandev.modest_meals.compat.farmers_delight;

import com.evandev.modest_meals.food.ingredient.IngredientProfileManager;
import com.evandev.modest_meals.food.meal.MealAssembler;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
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
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lets one {@link MealType} be cooked in Farmer's Delight's cooking pot.
 */
public class MealCookingRecipe extends CookingPotRecipe {

    private final ResourceLocation mealTypeId;
    private final int cookTime;

    public MealCookingRecipe(ResourceLocation mealTypeId, int cookTime) {
        super("", null, NonNullList.create(), ItemStack.EMPTY, ItemStack.EMPTY, 0.0F, cookTime);
        this.mealTypeId = mealTypeId;
        this.cookTime = cookTime;
    }

    /**
     * The stacks in the pot's input slots, in slot order.
     */
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

    private boolean containerMatches(RecipeWrapper inventory, MealType type) {
        return type.resolveContainer()
                .map(container -> inventory.getItem(CookingPotBlockEntity.CONTAINER_SLOT).is(container))
                .orElse(true);
    }

    public ResourceLocation mealTypeId() {
        return mealTypeId;
    }

    private Optional<MealType> mealType() {
        return MealTypeManager.get(mealTypeId)
                .filter(type -> type.station() == MealType.Station.COOKING_POT);
    }

    /**
     * The flavor ingredients in the pot: everything in the input slots minus the stacks the meal type takes
     * as its base.
     */
    private Optional<MealType.Split> ingredientsFor(RecipeWrapper inventory, MealType type) {
        if (!containerMatches(inventory, type)) {
            return Optional.empty();
        }

        Optional<MealType.Split> split = type.splitBase(inputsOf(inventory));
        if (split.isEmpty()) {
            return Optional.empty();
        }

        List<ItemStack> ingredients = split.get().ingredients();
        if (ingredients.isEmpty() || !type.withinBudget(ingredients)) {
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
                && isBestMatch(inventory, mine.get());
    }

    @Override
    public ItemStack assemble(RecipeWrapper inventory, HolderLookup.Provider registries) {
        return mealType()
                .flatMap(type -> ingredientsFor(inventory, type)
                        .flatMap(split -> MealAssembler.assemble(type, split.ingredients(), split.base())))
                .orElse(ItemStack.EMPTY);
    }

    /**
     * The bowl or bottle the finished dish is served in, from the meal type.
     */
    @Override
    public ItemStack getOutputContainer() {
        return mealType()
                .flatMap(MealType::resolveContainer)
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return mealType().flatMap(MealType::resolveItem).map(ItemStack::new).orElse(ItemStack.EMPTY);
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
    public int getCookTime() {
        return cookTime;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FarmersDelightCompat.MEAL_COOKING_SERIALIZER.get();
    }

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

        @Override
        public MapCodec<MealCookingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MealCookingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
