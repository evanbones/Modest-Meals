package com.evandev.modest_meals.item;

import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.food.ingredient.IngredientProfileManager;
import com.evandev.modest_meals.food.ingredient.MealIngredientManager;
import com.evandev.modest_meals.food.meal.MealFormulaManager;
import com.evandev.modest_meals.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MealItem extends Item {

    public static final String NAME_FLAVOURED_KEY = "modest_meals.meal.name.flavoured";
    public static final String FLAVOUR_KEY_PREFIX = "modest_meals.flavour.";
    public static final String NAME_PLAIN_KEY = "modest_meals.meal.name.plain";
    public static final String NAME_DUBIOUS_KEY = "modest_meals.meal.name.dubious";
    public static final String NAME_SLICE_KEY = "modest_meals.meal.name.slice";
    private static final float SATURATION_MODIFIER = 0.6F;
    private final UseAnim useAnimation;

    public MealItem(Properties properties) {
        this(properties, UseAnim.EAT);
    }

    public MealItem(Properties properties, UseAnim useAnimation) {
        super(properties);
        this.useAnimation = useAnimation;
    }

    public static MealContents contentsOf(ItemStack stack) {
        MealContents contents = stack.get(ModDataComponents.MEAL_CONTENTS.get());
        return contents == null ? MealContents.EMPTY : contents;
    }

    public static ItemStack makeDubious(Item item) {
        ItemStack stack = new ItemStack(item);
        MealContents contents = new MealContents(
                0.0F, 0.0F, 0, 0.0F,
                Optional.empty(), 0, 0,
                List.of(),
                List.of(),
                true
        );
        stack.set(ModDataComponents.MEAL_CONTENTS.get(), contents);
        return stack;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return contentsOf(stack).isMeaningful() ? useAnimation : super.getUseAnimation(stack);
    }

    @Override
    public @Nullable FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        MealContents contents = contentsOf(stack);
        if (!contents.isMeaningful()) {
            return super.getFoodProperties(stack, entity);
        }
        int nutrition = Math.max(1, Mth.ceil(contents.health()));

        boolean alwaysEdible = contents.health() <= 0.0F && contents.stamina() <= 0.0F;
        float eatSeconds = IngredientProfileManager.eatSeconds(this)
                .orElseGet(() -> MealFormulaManager.get().eatSecondsFor(contents.health() + contents.stamina()));

        return new FoodProperties(
                nutrition,
                FoodConstants.saturationByModifier(nutrition, SATURATION_MODIFIER),
                alwaysEdible,
                eatSeconds,
                Optional.ofNullable(getCraftingRemainingItem()).map(ItemStack::new),
                List.of());
    }

    @Override
    public Component getName(ItemStack stack) {
        return ModItems.portionOf(this)
                .filter(ModItems.Portion::sliceOfWhole)
                .map(portion -> (Component) Component.translatable(NAME_SLICE_KEY,
                        nameOf(stack, Component.translatable(portion.whole().get().getDescriptionId()))))
                .orElseGet(() -> nameOf(stack, Component.translatable(this.getDescriptionId(stack))));
    }

    public static Component nameOf(ItemStack stack, Component base) {
        MealContents contents = contentsOf(stack);
        if (contents.dubious()) {
            return Component.translatable(NAME_DUBIOUS_KEY, base);
        }
        return mainFlavour(stack, contents)
                .map(flavour -> (Component) Component.translatable(NAME_FLAVOURED_KEY, flavour, base))
                .orElseGet(() -> Component.translatable(NAME_PLAIN_KEY, base));
    }

    private static Optional<Component> mainFlavour(ItemStack stack, MealContents contents) {
        ResourceLocation mealId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return mainIngredient(contents.ingredients())
                .flatMap(BuiltInRegistries.ITEM::getOptional)
                .flatMap(item -> MealIngredientManager.familyOf(mealId, item))
                .map(family -> Component.translatable(FLAVOUR_KEY_PREFIX + family));
    }

    public static Optional<ResourceLocation> mainIngredient(List<ResourceLocation> ingredients) {
        Map<ResourceLocation, Integer> counts = new LinkedHashMap<>();
        ingredients.forEach(id -> counts.merge(id, 1, Integer::sum));
        return counts.entrySet().stream()
                .reduce((best, next) -> next.getValue() > best.getValue() ? next : best)
                .map(Map.Entry::getKey);
    }
}
