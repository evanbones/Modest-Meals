package com.evandev.modest_meals.compat.emi;

//? if cultural_delights && emi {
import com.baisylia.culturaldelights.integration.emi.AbstractVatRecipe;
import com.baisylia.culturaldelights.integration.emi.EMICulturalDelightsPlugin;
import com.baisylia.culturaldelights.util.VatTemperature;
import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.item.MealItem;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public class VatMealEmiRecipe extends MealEmiRecipe {

    private static final int VAT_SLOTS = 6;
    private static final int COLUMNS = 2;
    private static final int SLOTS_X = 19, SLOTS_Y = 2;

    private final int cookTicks;
    private final VatTemperature temperature;
    private final EmiStack container;

    private VatMealEmiRecipe(ResourceLocation id, MealType type, EmiIngredient any, List<EmiIngredient> slots,
                             int cookTicks, VatTemperature temperature) {
        super(EMICulturalDelightsPlugin.AGING, id, type, any, slots);
        this.cookTicks = cookTicks;
        this.temperature = temperature;
        this.container = containerStack();
    }

    private VatMealEmiRecipe(ResourceLocation id, MealType type, EmiIngredient any, List<EmiIngredient> slots,
                             int cookTicks, VatTemperature temperature, EmiStack output) {
        super(EMICulturalDelightsPlugin.AGING, id, type, any, slots, output);
        this.cookTicks = cookTicks;
        this.temperature = temperature;
        this.container = containerStack();
    }

    public static VatMealEmiRecipe of(MealType type, int cookTicks, VatTemperature temperature, List<EmiStack> pool) {
        List<EmiStack> typePool = MealEmiIngredients.poolFor(type, pool);
        EmiIngredient any = typePool.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(typePool);
        return new VatMealEmiRecipe(syntheticId("meal_chilling", type), type, any,
                flatSlots(type, any, VAT_SLOTS), cookTicks, temperature);
    }

    public static VatMealEmiRecipe dubiousOf(MealType type, int cookTicks, VatTemperature temperature,
                                             List<EmiStack> pool) {
        List<EmiStack> unsupported = MealEmiIngredients.unsupportedPoolFor(type, pool);
        EmiIngredient any = unsupported.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(unsupported);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                "/meal_chilling_dubious/" + type.id().getNamespace() + "/" + type.id().getPath());
        EmiStack output = type.resolveItem().map(item -> EmiStack.of(MealItem.makeDubious(item))).orElse(EmiStack.EMPTY);
        return new VatMealEmiRecipe(id, type, any, flatSlots(type, any, VAT_SLOTS), cookTicks, temperature, output);
    }

    public static Optional<VatMealEmiRecipe> overrideOf(MealType type, MealType.ItemOverride override, int index,
                                                        int cookTicks, VatTemperature temperature) {
        EmiStack output = overrideOutput(override);
        if (output.isEmpty()) {
            return Optional.empty();
        }
        return overrideFillings(override).map(fillings -> new VatMealEmiRecipe(
                overrideId("meal_chilling", type, index), type, null,
                fixedSlots(type, fillings, VAT_SLOTS), cookTicks, temperature, output));
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return container.isEmpty() ? List.of() : List.of(container);
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
        widgets.addTexture(AbstractVatRecipe.TEXTURE, 0, 0, 124, 58, 0, 0);

        for (int i = 0; i < VAT_SLOTS; i++) {
            EmiIngredient ingredient = slots.get(i);
            SlotWidget slot = widgets.addSlot(ingredient, SLOTS_X + i % COLUMNS * 18, SLOTS_Y + i / COLUMNS * 18);
            if (ingredient == any) {
                decorateFreeSlot(slot);
            }
        }

        SlotWidget containerSlot = widgets.addSlot(container, 95, 38);
        if (!container.isEmpty()) {
            containerSlot.appendTooltip(Component.translatable("recipe_viewer.modest_meals.container")
                    .withStyle(ChatFormatting.GRAY));
        }
        if (cookTicks > 0) {
            widgets.addFillingArrow(62, 10, cookTicks * 100)
                    .tooltipText(List.of(Component.translatable("emi.cooking.time", cookTicks / 20)));
        }
        widgets.addSlot(output, 95, 9).recipeContext(this).drawBack(false);

        int gaugeOffset = switch (temperature) {
            case COLD -> 0;
            case NORMAL -> 13;
            case HOT -> 26;
        };
        widgets.addTexture(AbstractVatRecipe.TEXTURE, 5, 6, 12, 46, 126 + gaugeOffset, 20)
                .tooltip((mouseX, mouseY) -> List.of(ClientTooltipComponent.create(
                        Component.translatable("container.culturaldelights.vat." + temperature.getSerializedName())
                                .getVisualOrderText())));
    }
}
//?}
