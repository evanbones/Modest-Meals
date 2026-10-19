package com.evandev.modest_meals.compat.emi;

import com.evandev.modest_meals.food.meal.MealType;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

public class VatMealEmiRecipe extends MealEmiRecipe {

    private static final int VAT_SLOTS = 6;
    private static final int COLUMNS = 3;
    private static final EmiStack COLD_SOURCE = EmiStack.of(Items.ICE);

    private final int cookTicks;
    private final EmiStack container;

    private VatMealEmiRecipe(ResourceLocation id, MealType type, EmiIngredient any, List<EmiIngredient> slots,
                             int cookTicks) {
        super(VatEmiSupport.MEAL_CHILLING, id, type, any, slots);
        this.cookTicks = cookTicks;
        this.container = containerStack();
    }

    private VatMealEmiRecipe(ResourceLocation id, MealType type, EmiIngredient any, List<EmiIngredient> slots,
                             int cookTicks, EmiStack output) {
        super(VatEmiSupport.MEAL_CHILLING, id, type, any, slots, output);
        this.cookTicks = cookTicks;
        this.container = containerStack();
    }

    public static VatMealEmiRecipe of(MealType type, int cookTicks, List<EmiStack> pool) {
        List<EmiStack> typePool = MealEmiIngredients.poolFor(type, pool);
        EmiIngredient any = typePool.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(typePool);
        return new VatMealEmiRecipe(syntheticId("meal_chilling", type), type, any,
                flatSlots(type, any, VAT_SLOTS), cookTicks);
    }

    public static Optional<VatMealEmiRecipe> overrideOf(MealType type, MealType.ItemOverride override, int index,
                                                        int cookTicks) {
        EmiStack output = overrideOutput(override);
        if (output.isEmpty()) {
            return Optional.empty();
        }
        return overrideFillings(override).map(fillings -> new VatMealEmiRecipe(
                overrideId("meal_chilling", type, index), type, null,
                fixedSlots(type, fillings, VAT_SLOTS), cookTicks, output));
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return container.isEmpty() ? List.of(COLD_SOURCE) : List.of(container, COLD_SOURCE);
    }

    @Override
    public int getDisplayWidth() {
        return 118;
    }

    @Override
    public int getDisplayHeight() {
        return 56;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int i = 0; i < VAT_SLOTS; i++) {
            EmiIngredient ingredient = slots.get(i);
            SlotWidget slot = widgets.addSlot(ingredient, i % COLUMNS * 18, i / COLUMNS * 18);
            if (ingredient == any) {
                decorateFreeSlot(slot);
            }
        }

        widgets.addFillingArrow(60, 9, cookTicks * 50)
                .tooltipText(List.of(Component.translatable("emi.cooking.time", cookTicks / 20)));
        widgets.addSlot(output, 92, 5).large(true).recipeContext(this);

        SlotWidget containerSlot = widgets.addSlot(container, 62, 38);
        if (!container.isEmpty()) {
            containerSlot.appendTooltip(Component.translatable("emi.modest_meals.container")
                    .withStyle(ChatFormatting.GRAY));
        }
        widgets.addSlot(COLD_SOURCE, 0, 38).catalyst(true)
                .appendTooltip(Component.translatable("emi.modest_meals.cold_vat").withStyle(ChatFormatting.AQUA));
        if (!isOverride()) {
            widgets.addTexture(EmiTexture.SHAPELESS, 97, 34);
        }
    }
}
