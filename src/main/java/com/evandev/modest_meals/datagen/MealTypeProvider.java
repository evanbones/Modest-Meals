package com.evandev.modest_meals.datagen;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class MealTypeProvider extends JsonDataProvider<MealType> {

    private static final TagKey<Item> BREADS = itemTag("c", "foods/bread");
    private static final TagKey<Item> DOUGHS = itemTag("c", "foods/dough");
    private static final TagKey<Item> PASTAS = itemTag("c", "foods/pasta");
    private static final TagKey<Item> TORTILLAS = itemTag("c", "foods/tortilla");
    private static final TagKey<Item> SAUCES = itemTag("c", "foods/tomato_sauce");
    private static final TagKey<Item> MILKS = itemTag("c", "drinks/milk");
    private static final TagKey<Item> WHEATS = itemTag("c", "crops/wheat");
    private static final TagKey<Item> RICES = itemTag("c", "crops/rice");
    private static final TagKey<Item> LEAFY_GREENS = itemTag("c", "foods/leafy_green");
    private static final TagKey<Item> SOUP_BASES = itemTag("c", "foods/soup_base");
    private static final TagKey<Item> DRINK_BASES = itemTag("c", "drinks/drink_base");
    private static final TagKey<Item> PIE_CRUSTS = itemTag("c", "foods/pie_crust");

    public MealTypeProvider(PackOutput output) {
        super(output, "modest_meals/meal_types", MealType.CODEC, "Modest Meals meal types");
    }

    private static String any() {
        return MealType.ANY_INGREDIENT.toString();
    }

    private static String id(Item item) {
        return itemId(item).toString();
    }

    private static ResourceLocation itemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    private static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    @Override
    protected void collect(Map<ResourceLocation, MealType> entries) {
        put(entries, "drink", ModItems.DRINK, MealType.Station.COOKING_POT,
                List.of(new MealType.BaseEntry(Ingredient.of(DRINK_BASES), 1)),
                Optional.empty(), 1, 4, Optional.of(itemId(Items.GLASS_BOTTLE)), 10);
        put(entries, "curry", ModItems.CURRY, MealType.Station.COOKING_POT,
                List.of(new MealType.BaseEntry(Ingredient.of(RICES), 1)),
                Optional.empty(), 2, 5, Optional.of(itemId(Items.BOWL)), 25);
        put(entries, "jam", ModItems.JAM, MealType.Station.COOKING_POT,
                List.of(new MealType.BaseEntry(Ingredient.of(Items.SUGAR), 1)),
                Optional.empty(), 1, 3, Optional.of(itemId(Items.GLASS_BOTTLE)), 30);

        put(entries, "soup", ModItems.SOUP, MealType.Station.COOKING_POT,
                List.of(new MealType.BaseEntry(Ingredient.of(SOUP_BASES), 1)),
                Optional.empty(), 2, 5, Optional.of(itemId(Items.BOWL)), 0);
        put(entries, "salad", ModItems.SALAD, MealType.Station.CRAFTING_TABLE,
                List.of(new MealType.BaseEntry(Ingredient.of(Items.BOWL), 1),
                        new MealType.BaseEntry(Ingredient.of(LEAFY_GREENS), 1)),
                Optional.empty(), 1, 3, Optional.of(itemId(Items.BOWL)), 5);
        put(entries, "ramen", ModItems.RAMEN, MealType.Station.COOKING_POT,
                List.of(new MealType.BaseEntry(Ingredient.of(PASTAS), 1),
                        new MealType.BaseEntry(Ingredient.of(SOUP_BASES), 1)),
                Optional.empty(), 1, 4, Optional.of(itemId(Items.BOWL)), 20);
        noodleMeal(entries, "pasta", ModItems.PASTA, 1, 3, 15);

        baseBowlMeal(entries, "porridge", ModItems.PORRIDGE, Ingredient.of(WHEATS), 1, 3, 25);
        put(entries, "ice_cream", ModItems.ICE_CREAM, MealType.Station.CRAFTING_TABLE,
                List.of(new MealType.BaseEntry(Ingredient.of(Items.SNOWBALL), 1),
                        new MealType.BaseEntry(Ingredient.of(MILKS), 1),
                        new MealType.BaseEntry(Ingredient.of(Items.SUGAR), 1)),
                Optional.empty(), 1, 3, Optional.empty(), 30);
        ResourceLocation sandwichId = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "sandwich");
        entries.put(sandwichId, new MealType(
                sandwichId,
                itemId(ModItems.SANDWICH.get()),
                MealType.Station.CRAFTING_TABLE,
                List.of(),
                Optional.of(new MealType.Shape(List.of(" B ", "###", " B "), Map.of("B", "#" + BREADS.location(), "#", any()))),
                3,
                3,
                Optional.empty(),
                10,
                true,
                Optional.of(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/sandwich/dubious")),
                Optional.of(new MealType.BaseSprites(
                        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/sandwich/sandwich_bottom"),
                        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/sandwich/sandwich_top")
                )),
                List.of()
        ));
        shaped(entries, "wrap", ModItems.WRAP, 2, 3, 10,
                List.of("###", " T "), Map.of("T", "#" + TORTILLAS.location(), "#", any()));
        shaped(entries, "pizza", ModItems.PIZZA, 1, 5, 10,
                List.of("###", "#S#", "DDD"),
                Map.of("D", "#" + DOUGHS.location(), "S", "#" + SAUCES.location(), "#", any()));
        shaped(entries, "pie", ModItems.PIE, 2, 6, 10,
                List.of("###", "###", "SOS"),
                Map.of("O", "#" + PIE_CRUSTS.location(), "S", id(Items.SUGAR), "#", any()));
        shaped(entries, "sushi", ModItems.SUSHI, 1, 2, 10,
                List.of("K#", "R#"),
                Map.of("K", id(Items.DRIED_KELP), "R", "#" + RICES.location(), "#", any()));
        /*
        ResourceLocation cakeId = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "cake");
        entries.put(cakeId, new MealType(
                cakeId,
                itemId(ModItems.CAKE.get()),
                MealType.Station.CRAFTING_TABLE,
                List.of(),
                Optional.of(new MealType.Shape(
                        List.of("MMM", "SES", "###"),
                        Map.of("M", "#" + MILKS.location(), "S", id(Items.SUGAR), "E", id(Items.EGG), "#", any())
                )),
                3,
                3,
                Optional.empty(),
                10,
                false,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                List.of(new MealType.ItemOverride(
                        itemId(Items.CAKE),
                        1,
                        List.of("#" + WHEATS.location(), "#" + WHEATS.location(), "#" + WHEATS.location())
                ))
        ));
        */

        shaped(entries, "skewer", ModItems.SKEWER, 2, 2, 15,
                List.of("#", "#", "S"), Map.of("S", id(Items.STICK), "#", any()));
        shaped(entries, "mochi", ModItems.MOCHI, 1, 1, 12,
                List.of("D#D"), Map.of("D", "#" + DOUGHS.location(), "#", any()));
    }

    private void baseBowlMeal(Map<ResourceLocation, MealType> entries, String name, Supplier<Item> item,
                              Ingredient extra, int min, int max, int priority) {
        put(entries, name, item, MealType.Station.CRAFTING_TABLE,
                List.of(new MealType.BaseEntry(Ingredient.of(Items.BOWL), 1),
                        new MealType.BaseEntry(extra, 1)),
                Optional.empty(), min, max, Optional.of(itemId(Items.BOWL)), priority);
    }


    private void noodleMeal(Map<ResourceLocation, MealType> entries, String name, Supplier<Item> item,
                            int min, int max, int priority) {
        put(entries, name, item, MealType.Station.CRAFTING_TABLE,
                List.of(new MealType.BaseEntry(Ingredient.of(Items.BOWL), 1),
                        new MealType.BaseEntry(Ingredient.of(PASTAS), 1)),
                Optional.empty(), min, max, Optional.of(itemId(Items.BOWL)), priority);
    }

    private void shaped(Map<ResourceLocation, MealType> entries, String name, Supplier<Item> item,
                        int min, int max, int priority, List<String> pattern, Map<String, String> key) {
        put(entries, name, item, MealType.Station.CRAFTING_TABLE, List.of(),
                Optional.of(new MealType.Shape(pattern, key)), min, max, Optional.empty(), priority);
    }

    private void put(Map<ResourceLocation, MealType> entries, String name, Supplier<Item> item,
                     MealType.Station station, List<MealType.BaseEntry> base,
                     Optional<MealType.Shape> shape, int min, int max,
                     Optional<ResourceLocation> container, int priority) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
        entries.put(id, new MealType(id, itemId(item.get()), station, base, shape,
                min, max, container, priority));
    }
}
