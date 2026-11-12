package com.evandev.modest_meals.compat.rrv;

//? if rrv && cultural_delights {
/*import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import com.baisylia.culturaldelights.integration.rrv.recipe.VatClientRecipe;
import com.baisylia.culturaldelights.recipes.VatRecipe;
import com.evandev.modest_meals.compat.cultural_delights.VatMealRecipe;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

public class MealVatRrvRecipe extends VatClientRecipe {

    private static final int VAT_SLOTS = 6;
    private static final int CONTAINER_SLOT = 6;

    private static final MealRrvSlots.Layout LAYOUT = new MealRrvSlots.Layout() {
        @Override
        public List<MealRrvSlots.Slot> free(MealType type, MealRrvSlots.Slot any) {
            return MealRrvSlots.flat(type, any, VAT_SLOTS);
        }

        @Override
        public List<MealRrvSlots.Slot> fixed(MealType type, MealType.ItemOverride override, List<MealRrvSlots.Slot> fillings) {
            return MealRrvSlots.fixed(type, fillings, VAT_SLOTS);
        }
    };

    private final MealType type;
    private final MealRrvSlots.Variant variant;

    private MealVatRrvRecipe(MealType type, MealRrvSlots.Variant variant, VatMealRecipe backing) {
        super(holder(type, variant, backing));
        this.type = type;
        this.variant = variant;
    }

    public static List<MealVatRrvRecipe> of(VatMealRecipe backing, List<ItemStack> pool) {
        return MealTypeManager.get(backing.mealTypeId())
                .map(type -> MealRrvSlots.variants("meal_chilling", type, pool, LAYOUT).stream()
                        .map(variant -> new MealVatRrvRecipe(type, variant, backing))
                        .toList())
                .orElse(List.of());
    }

    private static RecipeHolder<VatRecipe> holder(MealType type, MealRrvSlots.Variant variant, VatMealRecipe backing) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        variant.slots().forEach(slot -> ingredients.add(slot.ingredient()));
        VatRecipe display = new VatRecipe(ItemStackTemplate.fromNonEmptyStack(variant.output()), ingredients,
                backing.getTemperature(), type.resolveContainer().map(Ingredient::of), backing.getCookTime());
        return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, variant.id()), display);
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
