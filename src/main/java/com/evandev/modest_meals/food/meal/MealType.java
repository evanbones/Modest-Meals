package com.evandev.modest_meals.food.meal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A kind of dish: what item a set of ingredients turns into, and where and how it is assembled.
 */
public record MealType(
        ResourceLocation id,
        ResourceLocation item,
        Station station,
        List<BaseEntry> base,
        Optional<Shape> shape,
        int minIngredients,
        int maxIngredients,
        Optional<ResourceLocation> container,
        int priority
) {
    public static final ResourceLocation ANY_INGREDIENT =
            ResourceLocation.fromNamespaceAndPath("modest_meals", "any_ingredient");

    public static final Codec<MealType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(MealType::id),
            ResourceLocation.CODEC.fieldOf("item").forGetter(MealType::item),
            Station.CODEC.optionalFieldOf("station", Station.CRAFTING_TABLE).forGetter(MealType::station),
            BaseEntry.CODEC.listOf().optionalFieldOf("base", List.of()).forGetter(MealType::base),
            Shape.CODEC.optionalFieldOf("shape").forGetter(MealType::shape),
            Codec.INT.optionalFieldOf("min_ingredients", 1).forGetter(MealType::minIngredients),
            Codec.INT.optionalFieldOf("max_ingredients", 5).forGetter(MealType::maxIngredients),
            ResourceLocation.CODEC.optionalFieldOf("container").forGetter(MealType::container),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(MealType::priority)
    ).apply(instance, MealType::new));

    private static int indexMatching(List<ItemStack> stacks, Ingredient ingredient) {
        for (int i = 0; i < stacks.size(); i++) {
            if (ingredient.test(stacks.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public boolean isShaped() {
        return shape.isPresent();
    }

    public Optional<List<ItemStack>> stripBase(List<ItemStack> stacks) {
        List<ItemStack> remaining = new ArrayList<>(stacks);
        for (BaseEntry entry : base) {
            for (int consumed = 0; consumed < entry.count(); consumed++) {
                int index = indexMatching(remaining, entry.ingredient());
                if (index < 0) {
                    return Optional.empty();
                }
                remaining.remove(index);
            }
        }
        return Optional.of(remaining);
    }

    public boolean withinBudget(List<ItemStack> ingredients) {
        return ingredients.size() >= minIngredients && ingredients.size() <= maxIngredients;
    }

    public Optional<Item> resolveItem() {
        return BuiltInRegistries.ITEM.getOptional(item);
    }

    public Optional<Item> resolveContainer() {
        return container.flatMap(BuiltInRegistries.ITEM::getOptional);
    }

    /**
     * Where a type can be assembled.
     */
    public enum Station implements StringRepresentable {
        CRAFTING_TABLE("crafting_table"),
        COOKING_POT("cooking_pot");

        public static final Codec<Station> CODEC = StringRepresentable.fromEnum(Station::values);

        private final String name;

        Station(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record BaseEntry(Ingredient ingredient, int count) {
        public static final Codec<BaseEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(BaseEntry::ingredient),
                Codec.INT.optionalFieldOf("count", 1).forGetter(BaseEntry::count)
        ).apply(instance, BaseEntry::new));
    }

    /**
     * A crafting-grid layout, matching vanilla's {@code pattern}/{@code key} shape.
     */
    public record Shape(List<String> pattern, Map<String, String> key) {
        public static final Codec<Shape> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.listOf().fieldOf("pattern").forGetter(Shape::pattern),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("key").forGetter(Shape::key)
        ).apply(instance, Shape::new));

        public int width() {
            return pattern.stream().mapToInt(String::length).max().orElse(0);
        }

        public int height() {
            return pattern.size();
        }

        public char symbolAt(int row, int column) {
            if (row < 0 || row >= pattern.size()) {
                return ' ';
            }
            String line = pattern.get(row);
            return column < 0 || column >= line.length() ? ' ' : line.charAt(column);
        }

        public boolean isAny(char symbol) {
            return ANY_INGREDIENT.toString().equals(key.get(String.valueOf(symbol)));
        }

        public Optional<Ingredient> pinnedAt(char symbol) {
            String value = key.get(String.valueOf(symbol));
            if (value == null || ANY_INGREDIENT.toString().equals(value)) {
                return Optional.empty();
            }
            if (value.startsWith("#")) {
                return Optional.of(Ingredient.of(TagKey.create(
                        Registries.ITEM, ResourceLocation.parse(value.substring(1)))));
            }
            return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(value)).map(Ingredient::of);
        }
    }
}
