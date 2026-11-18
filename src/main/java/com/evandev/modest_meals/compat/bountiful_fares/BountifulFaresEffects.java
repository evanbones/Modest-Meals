package com.evandev.modest_meals.compat.bountiful_fares;

import com.evandev.modest_meals.food.FoodEffect;
import com.evandev.modest_meals.platform.Platform;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BountifulFaresEffects {
    private static final String MOD_ID = "bountifulfares";
    private static final String PACKAGE = "net.hecco.bountifulfares.";

    private static final Map<Class<?>, Optional<Field>> EFFECTS_FIELDS = new ConcurrentHashMap<>();
    private static Boolean loaded;

    public static List<FoodEffect> missingFrom(List<FoodEffect> effects, ItemStack stack) {
        List<FoodEffect> extra = get(stack);
        if (extra.isEmpty()) {
            return extra;
        }
        Set<Holder<MobEffect>> listed = effects.stream()
                .map(effect -> effect.effect().getEffect())
                .collect(Collectors.toSet());
        return extra.stream()
                .filter(effect -> !listed.contains(effect.effect().getEffect()))
                .toList();
    }

    private static List<FoodEffect> get(ItemStack stack) {
        if (!isLoaded()) {
            return List.of();
        }
        Item item = stack.getItem();
        Optional<Field> field = EFFECTS_FIELDS.computeIfAbsent(item.getClass(), BountifulFaresEffects::effectsField);
        if (field.isEmpty()) {
            return List.of();
        }
        try {
            if (field.get().get(item) instanceof List<?> effects) {
                return effects.stream()
                        .filter(MobEffectInstance.class::isInstance)
                        .map(effect -> new FoodEffect((MobEffectInstance) effect, 1.0F))
                        .toList();
            }
        } catch (IllegalAccessException ignored) {
        }
        return List.of();
    }

    private static boolean isLoaded() {
        if (loaded == null) {
            loaded = Platform.isModLoaded(MOD_ID);
        }
        return loaded;
    }

    private static Optional<Field> effectsField(Class<?> itemClass) {
        if (!itemClass.getName().startsWith(PACKAGE)) {
            return Optional.empty();
        }
        try {
            Field field = itemClass.getField("effects");
            return List.class.isAssignableFrom(field.getType()) ? Optional.of(field) : Optional.empty();
        } catch (NoSuchFieldException e) {
            return Optional.empty();
        }
    }
}
