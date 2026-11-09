package com.evandev.modest_meals.compat.emi;

//? if emi {
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public class MealCraftingHandler implements StandardRecipeHandler<CraftingMenu> {
    private static final int OUTPUT_SLOT = 0;
    private static final int FIRST_GRID_SLOT = 1;
    private static final int GRID_SLOTS = 9;
    private static final int INVENTORY_SLOTS = 36;

    @Override
    public List<Slot> getInputSources(CraftingMenu menu) {
        return slots(menu, FIRST_GRID_SLOT, GRID_SLOTS + INVENTORY_SLOTS);
    }

    @Override
    public List<Slot> getCraftingSlots(CraftingMenu menu) {
        return slots(menu, FIRST_GRID_SLOT, GRID_SLOTS);
    }

    @Override
    public Slot getOutputSlot(CraftingMenu menu) {
        return menu.getSlot(OUTPUT_SLOT);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof CraftingMealEmiRecipe;
    }

    private static List<Slot> slots(CraftingMenu menu, int first, int count) {
        List<Slot> slots = new ArrayList<>();
        for (int i = first; i < first + count; i++) {
            slots.add(menu.getSlot(i));
        }
        return slots;
    }
}
//?}
