package com.evandev.modest_meals.compat.rrv;

//? if rrv {
/*import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.compat.viewer.MealViewerIngredients;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.item.MealItem;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MealRrvSlots {

    public record Slot(List<ItemStack> stacks, boolean free) {
        public static final Slot EMPTY = new Slot(List.of(), false);

        public boolean isEmpty() {
            return stacks.isEmpty();
        }

        public SlotContent content() {
            return isEmpty() ? SlotContent.of() : SlotContent.of(stacks);
        }

        public Ingredient ingredient() {
            return Ingredient.of(HolderSet.direct(stacks.stream()
                    .map(stack -> BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()))
                    .toList()));
        }
    }

    public record Variant(ResourceLocation id, List<Slot> slots, ItemStack output, boolean dubious) {
        public void decorateFreeSlots(MealType type, RecipeViewMenu.SlotFillContext context) {
            Component tooltip = freeSlotTooltip(type, dubious);
            for (int i = 0; i < slots.size(); i++) {
                if (slots.get(i).free()) {
                    context.addAdditionalStackModifier(i, (stack, lines) -> lines.add(tooltip));
                }
            }
        }
    }

    public interface Layout {
        List<Slot> free(MealType type, Slot any);

        List<Slot> fixed(MealType type, MealType.ItemOverride override, List<Slot> fillings);
    }

    public static List<Variant> variants(String kind, MealType type, List<ItemStack> pool, Layout layout) {
        List<Variant> variants = new ArrayList<>();
        Optional<ItemStack> output = type.resolveItem().map(item -> new ItemStack(item, type.servings()));
        if (output.isEmpty()) {
            return variants;
        }

        Slot any = new Slot(pool.stream().filter(stack -> MealViewerIngredients.fits(type, stack)).toList(), true);
        if (!any.isEmpty()) {
            variants.add(new Variant(id(kind, type), layout.free(type, any), output.get(), false));
        }

        if (type.requiresSupportedIngredients()) {
            List<ItemStack> unsupported = pool.stream().filter(stack -> MealViewerIngredients.isUnsupported(type, stack)).toList();
            Slot dubiousAny = new Slot(unsupported.isEmpty() ? List.of(MealViewerIngredients.unsupportedFallback()) : unsupported, true);
            ItemStack dubiousOutput = MealItem.makeDubious(type.resolveItem().get());
            variants.add(new Variant(id(kind + "_dubious", type), layout.free(type, dubiousAny), dubiousOutput, true));
        }

        List<MealType.ItemOverride> overrides = type.itemOverrides();
        for (int i = 0; i < overrides.size(); i++) {
            MealType.ItemOverride override = overrides.get(i);
            Optional<ItemStack> result = BuiltInRegistries.ITEM.getOptional(override.result())
                    .map(item -> new ItemStack(item, override.count()));
            Optional<List<Slot>> fillings = overrideFillings(override);
            if (result.isPresent() && fillings.isPresent()) {
                ResourceLocation id = id(kind + "_override", type).withSuffix("/" + i);
                variants.add(new Variant(id, layout.fixed(type, override, fillings.get()), result.get(), false));
            }
        }
        return variants;
    }

    public static List<Slot> flat(MealType type, Slot any, int capacity) {
        List<Slot> laid = new ArrayList<>(base(type));
        while (laid.size() > capacity) {
            laid.removeLast();
        }
        for (int i = 0; i < type.maxIngredients() && laid.size() < capacity; i++) {
            laid.add(any);
        }
        return laid;
    }

    public static List<Slot> fixed(MealType type, List<Slot> fillings, int capacity) {
        List<Slot> laid = new ArrayList<>(base(type));
        laid.addAll(fillings);
        while (laid.size() > capacity) {
            laid.removeLast();
        }
        return laid;
    }

    public static List<ItemStack> stacksFor(String spec) {
        List<ItemStack> stacks = new ArrayList<>();
        for (String option : spec.split("\\|")) {
            if (option.startsWith("#")) {
                BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(Registries.ITEM, ResourceLocation.parse(option.substring(1))))
                        .forEach(holder -> stacks.add(new ItemStack(holder.value())));
            } else {
                BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(option)).ifPresent(item -> stacks.add(new ItemStack(item)));
            }
        }
        return stacks;
    }

    public static Component containerTooltip() {
        return Component.translatable("recipe_viewer.modest_meals.container").withStyle(ChatFormatting.GRAY);
    }

    private static List<Slot> base(MealType type) {
        List<Slot> base = new ArrayList<>();
        for (MealType.BaseEntry entry : type.base()) {
            for (int i = 0; i < entry.count(); i++) {
                base.add(new Slot(entry.active().displayStacks(), false));
            }
        }
        return base;
    }

    private static Optional<List<Slot>> overrideFillings(MealType.ItemOverride override) {
        List<Slot> fillings = override.ingredients().stream().map(spec -> new Slot(stacksFor(spec), false)).toList();
        return fillings.stream().anyMatch(Slot::isEmpty) ? Optional.empty() : Optional.of(fillings);
    }

    private static ResourceLocation id(String kind, MealType type) {
        return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,
                "/" + kind + "/" + type.id().getNamespace() + "/" + type.id().getPath());
    }

    private static Component freeSlotTooltip(MealType type, boolean dubious) {
        String key = dubious
                ? "recipe_viewer.modest_meals.unsupported_ingredient"
                : (type.requiresSupportedIngredients()
                ? "recipe_viewer.modest_meals.supported_ingredient"
                : "recipe_viewer.modest_meals.any_ingredient");
        return Component.translatable(key).withStyle(ChatFormatting.YELLOW);
    }
}
*///?}
