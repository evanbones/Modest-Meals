package com.evandev.modest_meals.compat.rrv;

//? if rrv && farmers_delight {
/*import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import com.evandev.modest_meals.food.meal.MealType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import vectorwing.farmersdelight.integration.rrv.cooking_pot.CookingPotClientRecipe;

import java.util.List;

public class MealCookingRrvRecipe extends CookingPotClientRecipe {

    private static final int POT_SLOTS = 6;
    private static final int CONTAINER_SLOT = 7;
    private static final int COOK_TIME = 200;

    private static final MealRrvSlots.Layout LAYOUT = new MealRrvSlots.Layout() {
        @Override
        public List<MealRrvSlots.Slot> free(MealType type, MealRrvSlots.Slot any) {
            return MealRrvSlots.flat(type, any, POT_SLOTS);
        }

        @Override
        public List<MealRrvSlots.Slot> fixed(MealType type, MealType.ItemOverride override, List<MealRrvSlots.Slot> fillings) {
            return MealRrvSlots.fixed(type, fillings, POT_SLOTS);
        }
    };

    private final MealType type;
    private final MealRrvSlots.Variant variant;

    private MealCookingRrvRecipe(MealType type, MealRrvSlots.Variant variant) {
        super(variant.id(),
                variant.slots().stream().map(MealRrvSlots.Slot::ingredient).toList(),
                ItemStackTemplate.fromNonEmptyStack(variant.output()),
                type.resolveContainer().map(ItemStackTemplate::new).orElse(null),
                0.0F, COOK_TIME);
        this.type = type;
        this.variant = variant;
    }

    public static List<MealCookingRrvRecipe> of(MealType type, List<ItemStack> pool) {
        return MealRrvSlots.variants("meal_cooking", type, pool, LAYOUT).stream()
                .map(variant -> new MealCookingRrvRecipe(type, variant))
                .toList();
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext context) {
        super.bindSlots(context);
        variant.decorateFreeSlots(type, context);
        if (type.resolveContainer().isPresent()) {
            context.addAdditionalStackModifier(CONTAINER_SLOT, (stack, lines) -> lines.add(MealRrvSlots.containerTooltip()));
        }
    }
}
*///?}
