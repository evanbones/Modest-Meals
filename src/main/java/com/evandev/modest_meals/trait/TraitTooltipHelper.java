package com.evandev.modest_meals.trait;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class TraitTooltipHelper {

    public static String formatDuration(long durationInTicks) {
        if (durationInTicks <= 0) return "";
        long totalSeconds = durationInTicks / 20;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes >= 60) {
            long hours = minutes / 60;
            minutes = minutes % 60;
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
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
