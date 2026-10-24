package com.evandev.modest_meals.datagen;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.registry.ModItems;
import com.evandev.modest_meals.tag.ModItemTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
    private static final TagKey<Item> COOKED_RICES = itemTag("c", "foods/cooked_rice");
    private static final TagKey<Item> VEGETABLES = itemTag("c", "foods/vegetable");
    private static final TagKey<Item> FRUITS = itemTag("c", "foods/fruit");
    private static final TagKey<Item> BERRIES = itemTag("c", "foods/berry");
    private static final TagKey<Item> RAW_MEATS = itemTag("c", "foods/raw_meat");

    public MealTypeProvider(PackOutput output) {
        super(output, "modest_meals/meal_types", MealType.CODEC, "Modest Meals meal types");
    }

    @SafeVarargs
    private static Ingredient tags(TagKey<Item>... tags) {
        return Ingredient.fromValues(Stream.of(tags).map(Ingredient.TagValue::new));
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
        Ingredient soupBase = withWaterBottle(SOUP_BASES);
        Ingredient bowl = Ingredient.of(Items.BOWL);

        Ingredient savoryOnly = tags(ModItemTags.NOT_FOR_DISHES, ModItemTags.SWEETS, ModItemTags.STAPLES);
        Ingredient savoryNoFruit = tags(ModItemTags.NOT_FOR_DISHES, ModItemTags.SWEETS, ModItemTags.STAPLES,
                ModItemTags.JAM_FRUITS);

        put(entries, "drink", ModItems.DRINK, MealType.Station.COOKING_POT,
                List.of(new MealType.BaseEntry(withWaterBottle(DRINK_BASES), 1)),
                Optional.empty(), 1, 4, Optional.of(itemId(Items.GLASS_BOTTLE)), 10,
                MealType.Rules.allowing(Ingredient.of(ModItemTags.DRINK_INGREDIENTS)));
        put(entries, "curry", ModItems.CURRY, MealType.Station.COOKING_POT,
                List.of(new MealType.BaseEntry(Ingredient.of(RICES), 1)),
                Optional.empty(), 2, 5, Optional.of(itemId(Items.BOWL)), 25,
                MealType.Rules.forbidding(savoryOnly));
        put(entries, "jam", ModItems.JAM, MealType.Station.COOKING_POT,
                List.of(new MealType.BaseEntry(Ingredient.of(Items.SUGAR), 1)),
                Optional.empty(), 1, 3, Optional.of(itemId(Items.GLASS_BOTTLE)), 30,
                MealType.Rules.allowing(Ingredient.of(ModItemTags.JAM_FRUITS))
                        .withForbidden(Ingredient.of(ModItemTags.NOT_FOR_JAM)));

        put(entries, "soup", ModItems.SOUP, MealType.Station.COOKING_POT,
                List.of(new MealType.BaseEntry(soupBase, 1)),
                Optional.empty(), 2, 5, Optional.of(itemId(Items.BOWL)), 0);
        put(entries, "salad", ModItems.SALAD, MealType.Station.CRAFTING_TABLE,
                List.of(new MealType.BaseEntry(bowl, 1),
                        new MealType.BaseEntry(Ingredient.of(LEAFY_GREENS), 1)),
                Optional.empty(), 1, 3, Optional.of(itemId(Items.BOWL)), 5,
                MealType.Rules.requiring(tags(VEGETABLES, LEAFY_GREENS, FRUITS, BERRIES))
                        .withForbidden(tags(ModItemTags.NOT_FOR_DISHES, ModItemTags.SWEETS, ModItemTags.STAPLES,
                                RAW_MEATS)));
        put(entries, "ramen", ModItems.RAMEN, MealType.Station.COOKING_POT,
                List.of(new MealType.BaseEntry(Ingredient.of(PASTAS), 1),
                        new MealType.BaseEntry(soupBase, 1)),
                Optional.empty(), 1, 4, Optional.of(itemId(Items.BOWL)), 20,
                MealType.Rules.forbidding(savoryNoFruit));
        put(entries, "pasta", ModItems.PASTA, MealType.Station.COOKING_POT,
                List.of(new MealType.BaseEntry(Ingredient.of(PASTAS), 1)),
                Optional.empty(), 1, 4, Optional.of(itemId(Items.BOWL)), 15,
                MealType.Rules.forbidding(savoryNoFruit));
        put(entries, "porridge", ModItems.PORRIDGE, MealType.Station.COOKING_POT,
                List.of(new MealType.BaseEntry(Ingredient.of(WHEATS), 1),
                        new MealType.BaseEntry(soupBase, 1)),
                Optional.empty(), 1, 3, Optional.of(itemId(Items.BOWL)), 25,
                MealType.Rules.forbidding(Ingredient.of(ModItemTags.NOT_FOR_DISHES)));

        put(entries, "ice_cream", ModItems.ICE_CREAM, MealType.Station.VAT,
                List.of(new MealType.BaseEntry(Ingredient.of(MILKS), 1),
                        new MealType.BaseEntry(Ingredient.of(Items.SUGAR), 1)),
                Optional.empty(), 1, 3, Optional.of(itemId(Items.BOWL)), 30,
                MealType.Rules.allowing(Ingredient.of(ModItemTags.DESSERT_INGREDIENTS)));
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
        ).withRules(MealType.Rules.forbidding(Ingredient.of(ModItemTags.NOT_FOR_DISHES))));
        ResourceLocation wrapId = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "wrap");
        entries.put(wrapId, new MealType(
                wrapId,
                itemId(ModItems.WRAP.get()),
                MealType.Station.CRAFTING_TABLE,
                List.of(new MealType.BaseEntry(Ingredient.of(TORTILLAS), 1)),
                Optional.empty(),
                3,
                3,
                Optional.empty(),
                10,
                true,
                Optional.of(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/wrap/dubious")),
                Optional.of(new MealType.BaseSprites(
                        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/wrap/wrap_base")
                )),
                List.of()
        ).withRules(MealType.Rules.forbidding(Ingredient.of(ModItemTags.NOT_FOR_DISHES))));
        ResourceLocation pizzaId = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "pizza");
        entries.put(pizzaId, new MealType(
                pizzaId,
                itemId(ModItems.PIZZA.get()),
                MealType.Station.CRAFTING_TABLE,
                List.of(),
                Optional.of(new MealType.Shape(List.of("###", "#S#", " D "),
                        Map.of("D", "#" + DOUGHS.location(), "S", "#" + ModItemTags.PIZZA_SAUCES.location(), "#", any()))),
                1,
                5,
                Optional.empty(),
                10,
                true,
                Optional.of(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/pizza/dubious")),
                Optional.of(new MealType.BaseSprites(
                        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/pizza/pizza_base")
                )),
                List.of()
        ).withRules(MealType.Rules.forbidding(tags(ModItemTags.NOT_FOR_DISHES, ModItemTags.SWEETS,
                ModItemTags.STAPLES, ModItemTags.JAM_FRUITS, MILKS))));
        /*
        shaped(entries, "pie", ModItems.PIE, 2, 6, 10,
                List.of("###", "###", "SOS"),
                Map.of("O", "#" + PIE_CRUSTS.location(), "S", id(Items.SUGAR), "#", any()));
        */
        put(entries, "roll", ModItems.ROLL, MealType.Station.CRAFTING_TABLE,
                List.of(new MealType.BaseEntry(Ingredient.of(COOKED_RICES), 1),
                        new MealType.BaseEntry(Ingredient.of(Items.DRIED_KELP), 1)),
                Optional.empty(), 1, 3, Optional.empty(), 10,
                MealType.Rules.allowing(Ingredient.of(ModItemTags.ROLL_FILLINGS)));
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


        DishOverrides.build().forEach((name, overrides) -> entries.computeIfPresent(
                ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                (id, type) -> type.withItemOverrides(overrides)));
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
        put(entries, name, item, station, base, shape, min, max, container, priority, MealType.Rules.NONE);
    }

    private void put(Map<ResourceLocation, MealType> entries, String name, Supplier<Item> item,
                     MealType.Station station, List<MealType.BaseEntry> base,
                     Optional<MealType.Shape> shape, int min, int max,
                     Optional<ResourceLocation> container, int priority, MealType.Rules rules) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
        entries.put(id, new MealType(id, itemId(item.get()), station, base, shape,
                min, max, container, priority).withRules(rules));
    }

    private static Ingredient withWaterBottle(TagKey<Item> liquids) {
        return CompoundIngredient.of(Ingredient.of(liquids), DataComponentIngredient.of(false,
                DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER), Items.POTION));
    }
}
