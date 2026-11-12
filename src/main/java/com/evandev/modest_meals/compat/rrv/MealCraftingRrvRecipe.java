package com.evandev.modest_meals.compat.rrv;

//? if rrv {
/*import cc.cassian.rrv.api.client.RecipeScreenContext;
import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.builtin.crafting.CraftingClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import com.evandev.modest_meals.food.meal.MealType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class MealCraftingRrvRecipe implements ReliableClientRecipe {

    private static final int GRID = 3;
    private static final int SLOTS = GRID * GRID;
    private static final int RESULT_SLOT = SLOTS;
    private static final ResourceLocation SHAPELESS = ResourceLocation.fromNamespaceAndPath("rrv", "crafting_shapeless");

    private static final MealRrvSlots.Layout LAYOUT = new MealRrvSlots.Layout() {
        @Override
        public List<MealRrvSlots.Slot> free(MealType type, MealRrvSlots.Slot any) {
            return type.isShaped()
                    ? shaped(type, symbol -> any, List.of())
                    : padded(MealRrvSlots.flat(type, any, SLOTS));
        }

        @Override
        public List<MealRrvSlots.Slot> fixed(MealType type, MealType.ItemOverride override, List<MealRrvSlots.Slot> fillings) {
            if (!type.isShaped()) {
                return padded(MealRrvSlots.fixed(type, fillings, SLOTS));
            }
            Iterator<MealRrvSlots.Slot> next = fillings.iterator();
            return shaped(type, symbol -> next.hasNext() ? next.next() : MealRrvSlots.Slot.EMPTY, override.base());
        }
    };

    private final MealType type;
    private final MealRrvSlots.Variant variant;
    private final List<SlotContent> grid;
    private final SlotContent result;

    private MealCraftingRrvRecipe(MealType type, MealRrvSlots.Variant variant) {
        this.type = type;
        this.variant = variant;
        this.grid = variant.slots().stream().map(MealRrvSlots.Slot::content).toList();
        this.result = SlotContent.of(variant.output());
    }

    public static List<MealCraftingRrvRecipe> of(MealType type, List<ItemStack> pool) {
        return MealRrvSlots.variants("meal_crafting", type, pool, LAYOUT).stream()
                .map(variant -> new MealCraftingRrvRecipe(type, variant))
                .toList();
    }

    private static List<MealRrvSlots.Slot> padded(List<MealRrvSlots.Slot> slots) {
        List<MealRrvSlots.Slot> laid = new ArrayList<>(slots);
        laid.addAll(Collections.nCopies(SLOTS - laid.size(), MealRrvSlots.Slot.EMPTY));
        return laid;
    }

    private static List<MealRrvSlots.Slot> shaped(MealType type, Function<Character, MealRrvSlots.Slot> free,
                                                  List<String> required) {
        MealType.Shape shape = type.shape().orElseThrow();
        List<MealRrvSlots.Slot> laid = new ArrayList<>(Collections.nCopies(SLOTS, MealRrvSlots.Slot.EMPTY));
        for (int row = 0; row < Math.min(GRID, shape.height()); row++) {
            for (int column = 0; column < Math.min(GRID, shape.width()); column++) {
                char symbol = shape.symbolAt(row, column);
                if (symbol == ' ') {
                    continue;
                }
                laid.set(row * GRID + column, shape.isAny(symbol) ? free.apply(symbol) : narrowed(key(shape, symbol), required));
            }
        }
        return laid;
    }

    private static MealRrvSlots.Slot key(MealType.Shape shape, char symbol) {
        String value = shape.key().get(String.valueOf(symbol));
        return value == null ? MealRrvSlots.Slot.EMPTY : new MealRrvSlots.Slot(MealRrvSlots.stacksFor(value), false);
    }

    private static MealRrvSlots.Slot narrowed(MealRrvSlots.Slot key, List<String> required) {
        for (String spec : required) {
            Predicate<ItemStack> matcher = MealType.ItemOverride.matcherFor(spec);
            if (key.stacks().stream().anyMatch(matcher)) {
                return new MealRrvSlots.Slot(MealRrvSlots.stacksFor(spec), false);
            }
        }
        return key;
    }

    @Override
    public ReliableClientRecipeType getType() {
        return CraftingClientRecipeType.INSTANCE;
    }

    @Override
    public ResourceLocation getId() {
        return variant.id();
    }

    @Override
    public void bindSlots(RecipeViewMenu.SlotFillContext context) {
        for (int i = 0; i < SLOTS; i++) {
            context.bindSlot(i, grid.get(i));
        }
        context.bindSlot(RESULT_SLOT, result);
        variant.decorateFreeSlots(type, context);
    }

    @Override
    public List<SlotContent> getIngredients() {
        return grid.stream().filter(content -> !content.isEmpty()).toList();
    }

    @Override
    public List<SlotContent> getResults() {
        return List.of(result);
    }

    @Override
    public boolean isVisualOnly() {
        return true;
    }

    @Override
    public void renderRecipe(RecipeScreenContext context) {
        if (type.isShaped()) {
            return;
        }
        context.guiGraphics().blitSprite(RenderPipelines.GUI_TEXTURED, SHAPELESS, 23, 14, 0, 0, 95, 3, 23, 14);
        if (context.mouseX() > 95 && context.mouseX() < 120 && context.mouseY() > 0 && context.mouseY() < 14) {
            context.guiGraphics().setComponentTooltipForNextFrame(context.font(),
                    List.of(Component.translatable("view.rrv.type.crafting.shapeless")),
                    context.mouseX() + context.recipePosition().left(), context.mouseY() + context.recipePosition().top());
        }
    }
}
*///?}
