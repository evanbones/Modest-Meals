package com.evandev.modest_meals.item;

import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.food.ingredient.MealEffect;
import com.evandev.modest_meals.food.ingredient.MealEffectManager;
import com.evandev.modest_meals.food.meal.MealFormulaManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A cooked dish. What a particular meal does lives in its {@link MealContents} component rather than in the item.
 */
public class MealItem extends Item {

    public static final String NAME_PREFIXED_KEY = "modest_meals.meal.name.prefixed";
    public static final String NAME_PLAIN_KEY = "modest_meals.meal.name.plain";
    public static final String NAME_DUBIOUS_KEY = "modest_meals.meal.name.dubious";
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
        float eatSeconds = MealFormulaManager.get().eatSecondsFor(contents.health() + contents.stamina());

        return new FoodProperties(
                nutrition,
                FoodConstants.saturationByModifier(nutrition, SATURATION_MODIFIER),
                alwaysEdible,
                eatSeconds,
                Optional.empty(),
                List.of());
    }

    @Override
    public Component getName(ItemStack stack) {
        MealContents contents = contentsOf(stack);
        Component base = Component.translatable(this.getDescriptionId(stack));
        if (contents.dubious()) {
            return Component.translatable(NAME_DUBIOUS_KEY, base);
        }
        Optional<Component> prefix = contents.effect()
                .flatMap(MealEffectManager::get)
                .map(MealEffect::namePrefixKeyOrDefault)
                .map(Component::translatable);
        return prefix
                .map(p -> (Component) Component.translatable(NAME_PREFIXED_KEY, p, base))
                .orElseGet(() -> Component.translatable(NAME_PLAIN_KEY, base));
    }
}
