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
        int priority,
        boolean requiresSupportedIngredients,
        Optional<ResourceLocation> dubiousSprite,
        Optional<BaseSprites> baseSprites,
        List<SpecialSprite> specialSprites,
        List<ItemOverride> itemOverrides
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
            Codec.INT.optionalFieldOf("priority", 0).forGetter(MealType::priority),
            Codec.BOOL.optionalFieldOf("requires_supported_ingredients", false).forGetter(MealType::requiresSupportedIngredients),
            ResourceLocation.CODEC.optionalFieldOf("dubious_sprite").forGetter(MealType::dubiousSprite),
            BaseSprites.CODEC.optionalFieldOf("base_sprites").forGetter(MealType::baseSprites),
            SpecialSprite.CODEC.listOf().optionalFieldOf("special_sprites", List.of()).forGetter(MealType::specialSprites),
            ItemOverride.CODEC.listOf().optionalFieldOf("item_overrides", List.of()).forGetter(MealType::itemOverrides)
    ).apply(instance, MealType::new));

    public MealType(
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
        this(id, item, station, base, shape, minIngredients, maxIngredients, container, priority,
                false, Optional.empty(), Optional.empty(), List.of(), List.of());
    }

    public MealType(
            ResourceLocation id,
            ResourceLocation item,
            Station station,
            List<BaseEntry> base,
            Optional<Shape> shape,
            int minIngredients,
            int maxIngredients,
            Optional<ResourceLocation> container,
            int priority,
            boolean requiresSupportedIngredients,
            Optional<ResourceLocation> dubiousSprite,
            Optional<BaseSprites> baseSprites,
            List<SpecialSprite> specialSprites
    ) {
        this(id, item, station, base, shape, minIngredients, maxIngredients, container, priority,
                requiresSupportedIngredients, dubiousSprite, baseSprites, specialSprites, List.of());
    }

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

    /**
     * Separates the stacks the type takes as its base from the free ingredients.
     */
    public Optional<Split> splitBase(List<ItemStack> stacks) {
        List<ItemStack> remaining = new ArrayList<>(stacks);
        List<ItemStack> taken = new ArrayList<>();
        for (BaseEntry entry : base) {
            for (int consumed = 0; consumed < entry.count(); consumed++) {
                int index = indexMatching(remaining, entry.ingredient());
                if (index < 0) {
                    return Optional.empty();
                }
                taken.add(remaining.remove(index));
            }
        }
        return Optional.of(new Split(taken, remaining));
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

    /**
     * A matched input split into the type's base stacks and the free ingredients chosen by the player.
     */
    public record Split(List<ItemStack> base, List<ItemStack> ingredients) {
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

    public record BaseSprites(
            Optional<ResourceLocation> bottom,
            Optional<ResourceLocation> top,
            Optional<Integer> layerSlots
    ) {
        public static final Codec<BaseSprites> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("bottom").forGetter(BaseSprites::bottom),
                ResourceLocation.CODEC.optionalFieldOf("top").forGetter(BaseSprites::top),
                Codec.INT.optionalFieldOf("layer_slots").forGetter(BaseSprites::layerSlots)
        ).apply(instance, BaseSprites::new));

        public BaseSprites(ResourceLocation bottom) {
            this(Optional.of(bottom), Optional.empty(), Optional.empty());
        }

        public BaseSprites(ResourceLocation bottom, ResourceLocation top) {
            this(Optional.of(bottom), Optional.of(top), Optional.empty());
        }

        public BaseSprites(ResourceLocation bottom, ResourceLocation top, int layerSlots) {
            this(Optional.of(bottom), Optional.of(top), Optional.of(layerSlots));
        }

        public int getEffectiveLayerSlots(int fallback) {
            return layerSlots.orElse(fallback);
        }
    }

    public record SpecialSprite(ResourceLocation sprite, List<ResourceLocation> ingredients) {
        public static final Codec<SpecialSprite> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("sprite").forGetter(SpecialSprite::sprite),
                ResourceLocation.CODEC.listOf().fieldOf("ingredients").forGetter(SpecialSprite::ingredients)
        ).apply(instance, SpecialSprite::new));

        public boolean matches(List<ResourceLocation> mealIngredients) {
            if (mealIngredients.size() != ingredients.size()) {
                return false;
            }
            List<String> sortedRequired = ingredients.stream().map(ResourceLocation::toString).sorted().toList();
            List<String> sortedGiven = mealIngredients.stream().map(ResourceLocation::toString).sorted().toList();
            return sortedRequired.equals(sortedGiven);
        }
    }

    public record ItemOverride(ResourceLocation result, int count, List<String> ingredients) {
        public static final Codec<ItemOverride> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("result").forGetter(ItemOverride::result),
                Codec.INT.optionalFieldOf("count", 1).forGetter(ItemOverride::count),
                Codec.STRING.listOf().fieldOf("ingredients").forGetter(ItemOverride::ingredients)
        ).apply(instance, ItemOverride::new));

        public ItemOverride(ResourceLocation result, List<String> ingredients) {
            this(result, 1, ingredients);
        }

        private static Ingredient parseIngredient(String spec) {
            if (spec.startsWith("#")) {
                return Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse(spec.substring(1))));
            }
            return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(spec))
                    .map(Ingredient::of)
                    .orElse(Ingredient.EMPTY);
        }

        public boolean matches(List<ItemStack> mealIngredients) {
            List<ItemStack> nonNull = mealIngredients.stream()
                    .filter(stack -> stack != null && !stack.isEmpty())
                    .toList();
            if (nonNull.size() != ingredients.size()) {
                return false;
            }
            List<Ingredient> matchers = ingredients.stream()
                    .map(ItemOverride::parseIngredient)
                    .toList();
            List<ItemStack> remaining = new ArrayList<>(nonNull);
            for (Ingredient matcher : matchers) {
                int found = -1;
                for (int i = 0; i < remaining.size(); i++) {
                    if (matcher.test(remaining.get(i))) {
                        found = i;
                        break;
                    }
                }
                if (found < 0) {
                    return false;
                }
                remaining.remove(found);
            }
            return true;
        }
    }
}
