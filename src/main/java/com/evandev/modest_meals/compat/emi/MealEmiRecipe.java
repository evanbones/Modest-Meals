package com.evandev.modest_meals.compat.emi;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.item.MealItem;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MealEmiRecipe implements EmiRecipe {

    protected final EmiRecipeCategory category;
    protected final ResourceLocation id;
    protected final MealType type;
    protected final EmiIngredient any;
    protected final List<EmiIngredient> slots;
    protected final EmiStack output;

    private final List<EmiIngredient> inputs;

    protected MealEmiRecipe(EmiRecipeCategory category, ResourceLocation id, MealType type,
                            EmiIngredient any, List<EmiIngredient> slots) {
        this(category, id, type, any, slots, type.resolveItem().map(EmiStack::of).orElse(EmiStack.EMPTY));
    }

    protected MealEmiRecipe(EmiRecipeCategory category, ResourceLocation id, MealType type,
                            EmiIngredient any, List<EmiIngredient> slots, EmiStack output) {
        this.category = category;
        this.id = id;
        this.type = type;
        this.any = any;
        this.slots = List.copyOf(slots);
        this.output = output;

        List<EmiIngredient> collected = new ArrayList<>();
        boolean pooled = false;
        for (EmiIngredient slot : this.slots) {
            if (slot.isEmpty()) {
                continue;
            }
            if (slot == any) {
                if (pooled) {
                    continue;
                }
                pooled = true;
            }
            collected.add(slot);
        }
        this.inputs = List.copyOf(collected);
    }

    protected static ResourceLocation syntheticId(String kind, MealType type) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                "/" + kind + "/" + type.id().getNamespace() + "/" + type.id().getPath());
    }

    protected static List<EmiIngredient> baseSlots(MealType type) {
        List<EmiIngredient> base = new ArrayList<>();
        for (MealType.BaseEntry entry : type.base()) {
            for (int i = 0; i < entry.count(); i++) {
                base.add(EmiIngredient.of(entry.ingredient()));
            }
        }
        return base;
    }

    protected static EmiIngredient keyIngredient(MealType.Shape shape, char symbol, EmiIngredient any) {
        String value = shape.key().get(String.valueOf(symbol));
        if (value == null) {
            return EmiStack.EMPTY;
        }
        if (MealType.ANY_INGREDIENT.toString().equals(value)) {
            return any;
        }
        if (value.startsWith("#")) {
            return EmiIngredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse(value.substring(1))));
        }
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(value))
                .<EmiIngredient>map(EmiStack::of)
                .orElse(EmiStack.EMPTY);
    }

    protected static List<EmiIngredient> flatSlots(MealType type, EmiIngredient any, int capacity) {
        List<EmiIngredient> laid = new ArrayList<>(baseSlots(type));
        while (laid.size() > capacity) {
            laid.removeLast();
        }
        for (int i = 0; i < type.maxIngredients() && laid.size() < capacity; i++) {
            laid.add(any);
        }
        laid.addAll(Collections.nCopies(capacity - laid.size(), EmiStack.EMPTY));
        return laid;
    }

    protected int poolSize() {
        return any.isEmpty() ? 0 : any.getEmiStacks().size();
    }

    protected boolean isDubious() {
        return MealItem.contentsOf(output.getItemStack()).dubious();
    }

    protected void decorateFreeSlot(SlotWidget slot) {
        String titleKey = isDubious()
                ? "emi.modest_meals.unsupported_ingredient"
                : (type.requiresSupportedIngredients()
                ? "emi.modest_meals.supported_ingredient"
                : "emi.modest_meals.any_ingredient");
        slot.appendTooltip(Component.translatable(titleKey)
                .withStyle(ChatFormatting.YELLOW));
        slot.appendTooltip(ingredientCountLine());
    }

    protected Component ingredientCountLine() {
        if (type.minIngredients() == type.maxIngredients()) {
            return Component.translatable("emi.modest_meals.ingredient_count.exact", type.minIngredients())
                    .withStyle(ChatFormatting.GRAY);
        }
        return Component.translatable("emi.modest_meals.ingredient_count.range",
                type.minIngredients(), type.maxIngredients()).withStyle(ChatFormatting.GRAY);
    }

    protected EmiStack containerStack() {
        return type.resolveContainer().map(EmiStack::of).orElse(EmiStack.EMPTY);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output.isEmpty() ? List.of() : List.of(output);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public boolean hideCraftable() {
        return true;
    }
}
