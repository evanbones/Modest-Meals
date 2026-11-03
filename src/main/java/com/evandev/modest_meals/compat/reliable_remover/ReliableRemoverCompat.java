package com.evandev.modest_meals.compat.reliable_remover;

import com.evandev.modest_meals.food.meal.MealType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ReliableRemoverCompat {
    public static final String MOD_ID = "reliable_remover";

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = ModList.get() != null && ModList.get().isLoaded(MOD_ID);
        }
        return loaded;
    }

    public static boolean isRemoved(Item item) {
        return isLoaded() && ReliableRemoverBridge.isRemoved(item);
    }

    public static boolean isHidden(Item item) {
        return isLoaded() && ReliableRemoverBridge.isHidden(item);
    }

    public static MealType prune(MealType type) {
        if (!isLoaded() || type.itemOverrides().isEmpty()) {
            return type;
        }
        List<MealType.ItemOverride> kept = new ArrayList<>();
        for (MealType.ItemOverride override : type.itemOverrides()) {
            prune(override).ifPresent(kept::add);
        }
        return kept.equals(type.itemOverrides()) ? type : type.withItemOverrides(kept);
    }

    private static Optional<MealType.ItemOverride> prune(MealType.ItemOverride override) {
        if (removed(override.result().toString())) {
            return Optional.empty();
        }
        Optional<List<String>> ingredients = pruneSpecs(override.ingredients());
        Optional<List<String>> base = pruneSpecs(override.base());
        if (ingredients.isEmpty() || base.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new MealType.ItemOverride(override.result(), override.count(), ingredients.get(),
                override.replaceRecipes(), base.get()));
    }

    private static Optional<List<String>> pruneSpecs(List<String> specs) {
        List<String> out = new ArrayList<>(specs.size());
        for (String spec : specs) {
            List<String> options = new ArrayList<>();
            for (String option : spec.split("\\|")) {
                if (option.startsWith("#") || !removed(option)) {
                    options.add(option);
                }
            }
            if (options.isEmpty()) {
                return Optional.empty();
            }
            out.add(String.join("|", options));
        }
        return Optional.of(out);
    }

    private static boolean removed(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            return false;
        }
        return BuiltInRegistries.ITEM.getOptional(location).map(ReliableRemoverCompat::isRemoved).orElse(false);
    }
}
