package com.evandev.modest_meals.trait;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class TraitTooltipHelper {

    public static String formatDuration(long durationInTicks, float tickRate) {
        if (durationInTicks <= 0) return "";
        return StringUtil.formatTickDuration((int) Math.min(durationInTicks, Integer.MAX_VALUE), tickRate);
    }

    public static MutableComponent formatPlusTrait(String translationKey, double value, long duration, float tickRate) {
        String amountText = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(Math.abs(value));
        MutableComponent line = Component.translatable("modest_meals.trait.modifier.plus",
                Component.literal(amountText).withStyle(ChatFormatting.BLUE),
                Component.translatable(translationKey).withStyle(ChatFormatting.BLUE)
        ).withStyle(ChatFormatting.BLUE);

        if (duration > 0) {
            line.append(Component.literal(" (" + formatDuration(duration, tickRate) + ")").withStyle(ChatFormatting.BLUE));
        }
        return line;
    }

    public static MutableComponent formatTakeTrait(String translationKey, double value, long duration, float tickRate) {
        String amountText = ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(Math.abs(value));
        MutableComponent line = Component.translatable("modest_meals.trait.modifier.take",
                Component.literal(amountText).withStyle(ChatFormatting.RED),
                Component.translatable(translationKey).withStyle(ChatFormatting.RED)
        ).withStyle(ChatFormatting.RED);

        if (duration > 0) {
            line.append(Component.literal(" (" + formatDuration(duration, tickRate) + ")").withStyle(ChatFormatting.RED));
        }
        return line;
    }

    public static MutableComponent formatSimple(String translationKey, long duration, float tickRate) {
        MutableComponent line = Component.translatable(translationKey).withStyle(ChatFormatting.BLUE);

        if (duration > 0) {
            line.append(Component.literal(" (" + formatDuration(duration, tickRate) + ")").withStyle(ChatFormatting.BLUE));
        }
        return line;
    }

    public static MutableComponent formatSimpleTake(String translationKey, long duration, float tickRate) {
        MutableComponent line = Component.translatable(translationKey).withStyle(ChatFormatting.RED);

        if (duration > 0) {
            line.append(Component.literal(" (" + formatDuration(duration, tickRate) + ")").withStyle(ChatFormatting.RED));
        }
        return line;
    }
}
