package com.evandev.modest_meals.compat.emi;

import com.evandev.modest_meals.food.ingredient.IngredientProfile;
import com.evandev.modest_meals.food.ingredient.IngredientProfileManager;
import com.evandev.modest_meals.food.ingredient.MealEffect;
import com.evandev.modest_meals.food.ingredient.MealEffectManager;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class MealEmiIngredients {

    private MealEmiIngredients() {
    }

    public static List<EmiStack> pool() {
        List<ItemStack> accepted = BuiltInRegistries.ITEM.stream()
                .map(ItemStack::new)
                .filter(IngredientProfileManager::isIngredient)
                .toList();

        return accepted.stream()
                .sorted(Comparator.comparingInt(MealEmiIngredients::interest))
                .map(EmiStack::of)
                .toList();
    }

    private static int interest(ItemStack stack) {
        Optional<IngredientProfile> authored = IngredientProfileManager.authored(stack);
        if (authored.filter(IngredientProfile::hasEffect).isPresent()) {
            return 0;
        }
        return authored.isPresent() ? 1 : 2;
    }

    public static List<Component> contributionLines(ItemStack stack) {
        IngredientProfile profile = IngredientProfileManager.resolve(stack).orElse(IngredientProfile.EMPTY);
        List<Component> lines = new ArrayList<>();

        addStat(lines, "emi.modest_meals.ingredient.health", profile.healthOrZero(), ChatFormatting.RED);
        addStat(lines, "emi.modest_meals.ingredient.stamina", profile.staminaOrZero(), ChatFormatting.GREEN);
        addStat(lines, "emi.modest_meals.ingredient.temporary_health", profile.temporaryHealth(),
                ChatFormatting.LIGHT_PURPLE);

        effectLine(profile).ifPresent(lines::add);

        if (profile.timeBonusSeconds() > 0) {
            lines.add(Component.translatable("emi.modest_meals.ingredient.time_bonus",
                    profile.timeBonusSeconds()).withStyle(ChatFormatting.GRAY));
        }

        if (lines.isEmpty()) {
            lines.add(Component.translatable("emi.modest_meals.ingredient.plain").withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }

    private static void addStat(List<Component> lines, String key, float value, ChatFormatting color) {
        if (value > 0.0F) {
            lines.add(Component.translatable(key, number(value)).withStyle(color));
        }
    }

    private static Optional<Component> effectLine(IngredientProfile profile) {
        if (!profile.hasEffect()) {
            return Optional.empty();
        }
        ResourceLocation axisId = profile.effect().orElseThrow();
        Component name = MealEffectManager.get(axisId)
                .map(MealEffect::displayName)
                .orElseGet(() -> Component.literal(axisId.getPath()));
        return Optional.of(Component
                .translatable("emi.modest_meals.ingredient.effect", name, profile.potency())
                .withStyle(ChatFormatting.YELLOW));
    }

    public static String number(float value) {
        return value == Math.rint(value)
                ? String.valueOf((int) value)
                : String.format(Locale.ROOT, "%.1f", value);
    }
}
