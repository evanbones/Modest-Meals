package com.evandev.modest_meals.compat.emi;

//? if cultural_delights && emi {
import com.baisylia.culturaldelights.integration.emi.AbstractOvenRecipe;
import com.baisylia.culturaldelights.integration.emi.EMICulturalDelightsPlugin;
import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.item.MealItem;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public class OvenMealEmiRecipe extends MealEmiRecipe {

    private final int cookTicks;
    private final boolean shapeless;

    private OvenMealEmiRecipe(ResourceLocation id, MealType type, EmiIngredient any, List<EmiIngredient> slots,
                              int cookTicks) {
        super(EMICulturalDelightsPlugin.SHAPED_BAKING, id, type, any, slots);
        this.cookTicks = cookTicks;
        this.shapeless = !type.isShaped();
    }

    private OvenMealEmiRecipe(ResourceLocation id, MealType type, EmiIngredient any, List<EmiIngredient> slots,
                              int cookTicks, EmiStack output) {
        super(EMICulturalDelightsPlugin.SHAPED_BAKING, id, type, any, slots, output);
        this.cookTicks = cookTicks;
        this.shapeless = !type.isShaped();
    }

    public static OvenMealEmiRecipe of(MealType type, int cookTicks, List<EmiStack> pool) {
        List<EmiStack> typePool = MealEmiIngredients.poolFor(type, pool);
        EmiIngredient any = typePool.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(typePool);
        return new OvenMealEmiRecipe(syntheticId("meal_baking", type), type, any, gridSlots(type, any), cookTicks);
    }

    public static OvenMealEmiRecipe dubiousOf(MealType type, int cookTicks, List<EmiStack> pool) {
        List<EmiStack> unsupported = MealEmiIngredients.unsupportedPoolFor(type, pool);
        EmiIngredient any = unsupported.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(unsupported);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                "/meal_baking_dubious/" + type.id().getNamespace() + "/" + type.id().getPath());
        EmiStack output = type.resolveItem().map(item -> EmiStack.of(MealItem.makeDubious(item))).orElse(EmiStack.EMPTY);
        return new OvenMealEmiRecipe(id, type, any, gridSlots(type, any), cookTicks, output);
    }

    public static Optional<OvenMealEmiRecipe> overrideOf(MealType type, MealType.ItemOverride override, int index,
                                                         int cookTicks) {
        EmiStack output = overrideOutput(override);
        if (output.isEmpty()) {
            return Optional.empty();
        }
        return overrideFillings(override).map(fillings -> new OvenMealEmiRecipe(
                overrideId("meal_baking", type, index), type, null,
                gridOverrideSlots(type, fillings, override.base()), cookTicks, output));
    }

    @Override
    public int getDisplayWidth() {
        return 124;
    }

    @Override
    public int getDisplayHeight() {
        return 58;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(AbstractOvenRecipe.TEXTURE, 0, 0, 124, 58, 0, 0);

        for (int i = 0; i < GRID_SLOTS; i++) {
            EmiIngredient ingredient = slots.get(i);
            SlotWidget slot = widgets.addSlot(ingredient, 2 + i % GRID * 18, 2 + i / GRID * 18).drawBack(false);
            if (ingredient == any) {
                decorateFreeSlot(slot);
            }
        }

        if (cookTicks > 0) {
            widgets.addFillingArrow(63, 20, cookTicks * 100)
                    .tooltipText(List.of(Component.translatable("emi.cooking.time", cookTicks / 20)));
        }
        if (shapeless) {
            widgets.addTexture(EmiTexture.SHAPELESS, 97, 0);
        }
        widgets.addSlot(output, 96, 20).recipeContext(this).drawBack(false);
    }
}
//?}
