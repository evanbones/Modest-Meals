package com.evandev.modest_meals.compat.emi;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.item.MealItem;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CraftingMealEmiRecipe extends MealEmiRecipe {

    private static final int GRID = 3;
    private static final int SLOTS = GRID * GRID;

    private final boolean shapeless;

    private CraftingMealEmiRecipe(MealType type, EmiIngredient any, List<EmiIngredient> slots) {
        this(type, any, slots, syntheticId("meal_crafting", type),
                type.resolveItem().map(item -> EmiStack.of(item, type.servings())).orElse(EmiStack.EMPTY));
    }

    private CraftingMealEmiRecipe(MealType type, EmiIngredient any, List<EmiIngredient> slots, ResourceLocation id, EmiStack output) {
        super(MealEmiCategories.MEAL_CRAFTING, id, type, any, slots, output);
        this.shapeless = !type.isShaped();
    }

    public static CraftingMealEmiRecipe of(MealType type, List<EmiStack> pool) {
        List<EmiStack> typePool = MealEmiIngredients.poolFor(type, pool);
        EmiIngredient any = typePool.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(typePool);
        return new CraftingMealEmiRecipe(type, any,
                type.isShaped() ? shapedSlots(type, any) : flatSlots(type, any, SLOTS));
    }

    public static CraftingMealEmiRecipe dubiousOf(MealType type, List<EmiStack> pool) {
        List<EmiStack> unsupported = MealEmiIngredients.unsupportedPoolFor(type, pool);
        EmiIngredient any = unsupported.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(unsupported);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                "/meal_crafting_dubious/" + type.id().getNamespace() + "/" + type.id().getPath());
        EmiStack output = type.resolveItem().map(item -> EmiStack.of(MealItem.makeDubious(item))).orElse(EmiStack.EMPTY);
        return new CraftingMealEmiRecipe(type, any,
                type.isShaped() ? shapedSlots(type, any) : flatSlots(type, any, SLOTS), id, output);
    }

    public static Optional<CraftingMealEmiRecipe> overrideOf(MealType type, MealType.ItemOverride override, int index) {
        EmiStack output = overrideOutput(override);
        if (output.isEmpty()) {
            return Optional.empty();
        }
        return overrideFillings(override).map(fillings -> new CraftingMealEmiRecipe(type, null,
                type.isShaped() ? shapedSlots(type, fillings, override.base()) : fixedSlots(type, fillings, SLOTS),
                overrideId("meal_crafting", type, index), output));
    }

    private static List<EmiIngredient> shapedSlots(MealType type, EmiIngredient any) {
        return shapedSlots(type, symbol -> any);
    }

    private static List<EmiIngredient> shapedSlots(MealType type, List<EmiIngredient> fillings, List<String> base) {
        Iterator<EmiIngredient> next = fillings.iterator();
        List<Ingredient> required = base.stream().map(MealType.ItemOverride::parseIngredient).toList();
        return shapedSlots(type, symbol -> next.hasNext() ? next.next() : EmiStack.EMPTY, required);
    }

    private static List<EmiIngredient> shapedSlots(MealType type, Function<Character, EmiIngredient> free) {
        return shapedSlots(type, free, List.of());
    }

    /**
     * @param required base stacks an override needs; a key slot that can hold one shows just that instead of its whole key
     */
    private static List<EmiIngredient> shapedSlots(MealType type, Function<Character, EmiIngredient> free,
                                                   List<Ingredient> required) {
        MealType.Shape shape = type.shape().orElseThrow();
        List<EmiIngredient> laid = new ArrayList<>(Collections.nCopies(SLOTS, (EmiIngredient) EmiStack.EMPTY));
        for (int row = 0; row < Math.min(GRID, shape.height()); row++) {
            for (int column = 0; column < Math.min(GRID, shape.width()); column++) {
                char symbol = shape.symbolAt(row, column);
                if (symbol == ' ') {
                    continue;
                }
                laid.set(row * GRID + column, shape.isAny(symbol) ? free.apply(symbol) : narrowed(keyIngredient(shape, symbol, EmiStack.EMPTY), required));
            }
        }
        return laid;
    }

    private static EmiIngredient narrowed(EmiIngredient key, List<Ingredient> required) {
        for (Ingredient ingredient : required) {
            boolean overlaps = key.getEmiStacks().stream()
                    .anyMatch(stack -> ingredient.test(stack.getItemStack()));
            if (overlaps) {
                return EmiIngredient.of(ingredient);
            }
        }
        return key;
    }

    @Override
    public int getDisplayWidth() {
        return 118;
    }

    @Override
    public int getDisplayHeight() {
        return 54;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int i = 0; i < SLOTS; i++) {
            EmiIngredient ingredient = slots.get(i);
            SlotWidget slot = widgets.addSlot(ingredient, i % GRID * 18, i / GRID * 18);
            if (ingredient == any) {
                decorateFreeSlot(slot);
            }
        }

        widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 18);
        if (shapeless) {
            widgets.addTexture(EmiTexture.SHAPELESS, 97, 0);
        }

        widgets.addSlot(output, 92, 14).large(true).recipeContext(this);
    }
}
