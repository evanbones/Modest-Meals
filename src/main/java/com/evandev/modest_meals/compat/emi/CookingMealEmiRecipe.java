package com.evandev.modest_meals.compat.emi;

import com.evandev.modest_meals.food.meal.MealType;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import vectorwing.farmersdelight.common.utility.ClientRenderUtils;
import vectorwing.farmersdelight.common.utility.RecipeUtils;

import java.util.ArrayList;
import java.util.List;

public class CookingMealEmiRecipe extends MealEmiRecipe {

    private static final ResourceLocation BACKGROUND = RecipeUtils.FDLocation("textures/gui/jei/cooking_pot.png");
    private static final ResourceLocation WIDGETS = RecipeUtils.FDLocation("textures/gui/cooking_pot.png");

    private static final int POT_SLOTS = 6;
    private static final int COLUMNS = 3;

    private static final int INFO_X = 60, INFO_Y = 2, INFO_WIDTH = 22, INFO_HEIGHT = 28;

    private final int cookTicks;
    private final float experience;
    private final EmiStack container;

    private CookingMealEmiRecipe(ResourceLocation id, MealType type, EmiIngredient any,
                                 List<EmiIngredient> slots, int cookTicks, float experience) {
        super(CookingPotEmiSupport.MEAL_COOKING, id, type, any, slots);
        this.cookTicks = cookTicks;
        this.experience = experience;
        this.container = containerStack();
    }

    public static CookingMealEmiRecipe of(MealType type, int cookTicks, float experience, List<EmiStack> pool) {
        EmiIngredient any = pool.isEmpty() ? EmiStack.EMPTY : EmiIngredient.of(pool);
        return new CookingMealEmiRecipe(syntheticId("meal_cooking", type), type, any,
                flatSlots(type, any, POT_SLOTS), cookTicks, experience);
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return container.isEmpty() ? List.of() : List.of(container);
    }

    @Override
    public int getDisplayWidth() {
        return 116;
    }

    @Override
    public int getDisplayHeight() {
        return 56;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(BACKGROUND, 0, 0, 116, 56, 0, 0);

        for (int i = 0; i < POT_SLOTS; i++) {
            EmiIngredient ingredient = slots.get(i);
            SlotWidget slot = addSlot(widgets, ingredient, i % COLUMNS * 18, i / COLUMNS * 18);
            if (ingredient == any) {
                decorateFreeSlot(slot);
            }
        }

        decorateOutput(addSlot(widgets, output, 94, 9));
        SlotWidget containerSlot = addSlot(widgets, container, 62, 38);
        if (!container.isEmpty()) {
            containerSlot.appendTooltip(Component.translatable("emi.modest_meals.container")
                    .withStyle(ChatFormatting.GRAY));
        }
        decorateOutput(addSlot(widgets, output, 94, 38).recipeContext(this));

        widgets.addAnimatedTexture(WIDGETS, 60, 9, 24, 17, 176, 15, 1000 * 10, true, false, false);
        widgets.addTexture(WIDGETS, 18, 39, 17, 15, 176, 0);
        widgets.addTexture(WIDGETS, 64, 2, 8, 11, 176, 32);
        if (experience > 0) {
            widgets.addTexture(WIDGETS, 63, 21, 9, 9, 176, 43);
        }

        List<ClientTooltipComponent> info = infoTooltip();
        widgets.addTooltip((mouseX, mouseY) ->
                        ClientRenderUtils.isCursorInsideBounds(INFO_X, INFO_Y, INFO_WIDTH, INFO_HEIGHT, mouseX, mouseY)
                                ? info
                                : List.of(),
                0, 0, widgets.getWidth(), widgets.getHeight());
    }

    private SlotWidget addSlot(WidgetHolder widgets, EmiIngredient ingredient, int x, int y) {
        return widgets.addSlot(ingredient, x, y).drawBack(false);
    }

    private List<ClientTooltipComponent> infoTooltip() {
        List<Component> lines = new ArrayList<>();
        if (cookTicks > 0) {
            lines.add(Component.translatable("emi.cooking.time", cookTicks / 20));
        }
        if (experience > 0) {
            lines.add(Component.translatable("emi.cooking.experience", experience));
        }
        lines.add(ingredientCountLine());
        return lines.stream()
                .map(line -> ClientTooltipComponent.create(line.getVisualOrderText()))
                .toList();
    }
}
