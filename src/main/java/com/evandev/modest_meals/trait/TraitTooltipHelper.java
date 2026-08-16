package com.evandev.modest_meals.trait;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class TraitTooltipHelper {

    public static String formatDuration(long durationInTicks) {
        if (durationInTicks <= 0) return "";
        double seconds = durationInTicks / 20.0;
        if (seconds >= 60) {
            int mins = (int) (seconds / 60);
            int remSecs = (int) (seconds % 60);
            if (remSecs > 0) {
                return mins + "m " + remSecs + "s";
            }
            return mins + "m";
        }
        return ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(seconds) + "s";
    }

    public static MutableComponent formatPlusTrait(String translationKey, double value, long duration) {
        String amountText = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(Math.abs(value));
        MutableComponent line = Component.translatable("modest_meals.trait.modifier.plus",
                Component.literal(amountText).withStyle(ChatFormatting.BLUE),
                Component.translatable(translationKey).withStyle(ChatFormatting.BLUE)
        ).withStyle(ChatFormatting.BLUE);

        if (duration > 0) {
            line.append(Component.literal(" (" + formatDuration(duration) + ")").withStyle(ChatFormatting.GRAY));
        }
        return line;
    }

    public static MutableComponent formatTakeTrait(String translationKey, double value, long duration) {
        String amountText = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(Math.abs(value));
        MutableComponent line = Component.translatable("modest_meals.trait.modifier.take",
                Component.literal(amountText).withStyle(ChatFormatting.RED),
                Component.translatable(translationKey).withStyle(ChatFormatting.RED)
        ).withStyle(ChatFormatting.RED);

        if (duration > 0) {
            line.append(Component.literal(" (" + formatDuration(duration) + ")").withStyle(ChatFormatting.GRAY));
        }
        return line;
    }

    public static MutableComponent formatSimplePlus(String translationKey, long duration) {
        MutableComponent line = Component.translatable("modest_meals.trait.simple.plus",
                Component.translatable(translationKey).withStyle(ChatFormatting.BLUE)
        ).withStyle(ChatFormatting.BLUE);

        if (duration > 0) {
            line.append(Component.literal(" (" + formatDuration(duration) + ")").withStyle(ChatFormatting.GRAY));
        }
        return line;
    }

    public static MutableComponent formatSimpleTake(String translationKey, long duration) {
        MutableComponent line = Component.translatable("modest_meals.trait.simple.take",
                Component.translatable(translationKey).withStyle(ChatFormatting.RED)
        ).withStyle(ChatFormatting.RED);

        if (duration > 0) {
            line.append(Component.literal(" (" + formatDuration(duration) + ")").withStyle(ChatFormatting.GRAY));
        }
        return line;
    }
}
