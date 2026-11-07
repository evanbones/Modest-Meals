package com.evandev.modest_meals.food;

import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.food.meal.MealTraits;
import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitManager;
import com.evandev.modest_meals.trait.FoodTraitType;
import com.evandev.modest_meals.trait.TraitBenefit;
import com.evandev.modest_meals.trait.impl.EffectGrantTrait;
import com.evandev.modest_meals.trait.impl.HealthAdditionTrait;
import com.evandev.modest_meals.trait.impl.StaminaAdditionTrait;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FoodValues {

    public static List<FoodTrait> effectiveTraits(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return List.of();
        }
        return effectiveTraits(stack, nutritionOf(stack));
    }

    public static List<FoodTrait> effectiveTraits(ItemStack stack, int nutrition) {
        List<FoodTrait> traits = unfilteredTraits(stack, nutrition);
        if (ModConfig.get().foodEffects) {
            return traits;
        }
        return traits.stream().filter(trait -> !(trait instanceof EffectGrantTrait)).toList();
    }

    public static List<FoodTrait> displayTraits(ItemStack stack) {
        List<FoodTrait> traits = effectiveTraits(stack);
        MealContents contents = stack.get(ModDataComponents.MEAL_CONTENTS.get());
        if (!ModConfig.get().foodEffects || (contents != null && contents.isMeaningful())) {
            return traits;
        }
        return BuiltInFoodEffects.appendTo(stack, traits, FoodTraitManager.suppressionsFor(stack.getItem()));
    }

    private static List<FoodTrait> unfilteredTraits(ItemStack stack, int nutrition) {
        if (stack == null || stack.isEmpty()) {
            return FoodProfileManager.resolveDefault()
                    .map(profile -> withDerived(ItemStack.EMPTY, nutrition, List.of(), profile, Set.of()))
                    .orElseGet(List::of);
        }
        MealContents contents = stack.get(ModDataComponents.MEAL_CONTENTS.get());
        if (contents != null && contents.isMeaningful()) {
            return MealTraits.of(stack, contents);
        }
        return withDerived(stack, nutrition, FoodTraitManager.getTraits(stack),
                FoodProfileManager.resolve(stack).orElse(null),
                FoodTraitManager.suppressionsFor(stack.getItem()));
    }

    public static List<FoodTrait> withDerived(ItemStack stack, List<FoodTrait> authored,
                                              @Nullable FoodProfile profile, Set<String> suppressed) {
        return withDerived(stack, nutritionOf(stack), authored, profile, suppressed);
    }

    public static List<FoodTrait> withDerived(ItemStack stack, int nutrition, List<FoodTrait> authored,
                                              @Nullable FoodProfile profile, Set<String> suppressed) {
        boolean hasHealth = suppressed.contains(FoodTraitManager.mergeKeyOf(FoodTraitType.HEALTH_ADDITION));
        boolean hasStamina = suppressed.contains(FoodTraitManager.mergeKeyOf(FoodTraitType.STAMINA_ADDITION));
        for (FoodTrait trait : authored) {
            if (trait instanceof HealthAdditionTrait) {
                hasHealth = true;
            }
            if (trait instanceof StaminaAdditionTrait) {
                hasStamina = true;
            }
        }

        if (hasHealth && hasStamina) {
            return authored;
        }

        if (nutrition <= 0 || profile == null) {
            return authored;
        }

        List<FoodTrait> effective = new ArrayList<>(authored);
        if (!hasHealth) {
            float health = profile.healthFor(nutrition);
            if (health > 0.0F) {
                effective.add(new HealthAdditionTrait(health, profile.digestTicksFor(health)));
            }
        }
        if (!hasStamina) {
            float stamina = profile.staminaFor(nutrition);
            if (stamina > 0.0F) {
                effective.add(new StaminaAdditionTrait(stamina));
            }
        }
        return effective;
    }

    public static float healthPoints(ItemStack stack) {
        float total = 0.0F;
        for (FoodTrait trait : effectiveTraits(stack)) {
            if (trait instanceof HealthAdditionTrait healthAddition) {
                total += healthAddition.value();
            }
        }
        return total * ModConfig.get().traitGlobalValueMultiplier;
    }

    public static int healthDigestTicks(ItemStack stack) {
        int longest = 0;
        for (FoodTrait trait : effectiveTraits(stack)) {
            if (trait instanceof HealthAdditionTrait healthAddition) {
                longest = Math.max(longest, healthAddition.duration());
            }
        }
        return (int) (longest * ModConfig.get().traitGlobalDurationMultiplier);
    }

    public static float staminaLevels(ItemStack stack) {
        float total = 0.0F;
        for (FoodTrait trait : effectiveTraits(stack)) {
            if (trait instanceof StaminaAdditionTrait(float value)) {
                total += value;
            }
        }
        return total * ModConfig.get().traitGlobalValueMultiplier;
    }

    public static boolean touchesHealth(ItemStack stack) {
        return hasBenefit(stack, TraitBenefit.HEALTH);
    }

    public static boolean touchesStamina(ItemStack stack) {
        return ModConfig.get().staminaSprint && hasBenefit(stack, TraitBenefit.STAMINA);
    }

    public static boolean hasUtility(ItemStack stack) {
        return hasBenefit(stack, TraitBenefit.UTILITY);
    }

    private static boolean hasBenefit(ItemStack stack, TraitBenefit benefit) {
        for (FoodTrait trait : effectiveTraits(stack)) {
            if (trait.benefit() == benefit) {
                return true;
            }
        }
        return false;
    }

    public static int nutritionOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null) {
            return food.nutrition();
        }
        return EdibleBlockFoods.getFoodProperties(stack.getItem())
                .map(FoodProperties::nutrition)
                .orElse(0);
    }

    public static boolean isFood(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.has(DataComponents.FOOD) || EdibleBlockFoods.getFoodProperties(stack.getItem()).isPresent()) {
            return true;
        }
        MealContents contents = stack.get(ModDataComponents.MEAL_CONTENTS.get());
        return contents != null && contents.isMeaningful();
    }

    public static ItemStack resolveHeldFood(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (isFood(mainHand)) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        if (isFood(offHand)) {
            return offHand;
        }
        return ItemStack.EMPTY;
    }
}
