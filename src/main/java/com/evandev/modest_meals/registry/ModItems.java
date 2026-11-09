package com.evandev.modest_meals.registry;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.meal.MealType.Station;
import com.evandev.modest_meals.item.MealBlockItem;
import com.evandev.modest_meals.item.MealItem;
import com.evandev.modest_meals.platform.RegistrationProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

//? if >=26.1 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Consumables;
*///?}

public class ModItems {
    private static final RegistrationProvider<Item> ITEMS = RegistrationProvider.get(Registries.ITEM);
    private static final int MEAL_STACK_SIZE = 16;

    public static final Set<Supplier<Item>> MEALS = new LinkedHashSet<>();
    public static final Set<Supplier<Item>> CREATIVE_TAB_ITEMS = new LinkedHashSet<>();
    public static final Map<Supplier<Item>, Portion> PORTIONS = new LinkedHashMap<>();

    public static final Supplier<Item> SOUP = registerMeal("soup", Station.COOKING_POT, Items.BOWL, UseAnim.EAT);
    public static final Supplier<Item> RAMEN = registerMeal("ramen", Station.COOKING_POT, Items.BOWL, UseAnim.EAT);
    public static final Supplier<Item> PASTA = registerMeal("pasta", Station.COOKING_POT, Items.BOWL, UseAnim.EAT);
    public static final Supplier<Item> SALAD = registerMeal("salad", Station.CRAFTING_TABLE, Items.BOWL, UseAnim.EAT);
    public static final Supplier<Item> CURRY = registerMeal("curry", Station.COOKING_POT, Items.BOWL, UseAnim.EAT);
    public static final Supplier<Item> JAM = registerMeal("jam", Station.COOKING_POT, Items.GLASS_BOTTLE, UseAnim.EAT);

    public static final Supplier<Item> SANDWICH = registerMeal("sandwich", Station.CRAFTING_TABLE, null, UseAnim.EAT);
    public static final Supplier<Item> WRAP = registerMeal("wrap", Station.CRAFTING_TABLE, null, UseAnim.EAT);

    public static final Supplier<Item> PIZZA = registerBlockMeal("pizza", ModBlocks.PIZZA, MEAL_STACK_SIZE);
    public static final Supplier<Item> PIZZA_SLICE = registerPortion("pizza_slice", PIZZA, 4, true);
    public static final Supplier<Item> ROLL = registerMeal("roll", Station.CRAFTING_TABLE, null, UseAnim.EAT);
    public static final Supplier<Item> ROLL_SLICE = registerPortion("roll_slice", ROLL, 3, false);
    public static final Supplier<Item> ICE_CREAM = registerMeal("ice_cream", Station.VAT, null, UseAnim.EAT);
    public static final Supplier<Item> ICE_CREAM_BOWL = registerMeal("ice_cream_bowl", Station.VAT, Items.BOWL, UseAnim.EAT);
    public static final Supplier<Item> PIE = registerBlockMeal("pie", ModBlocks.PIE, MEAL_STACK_SIZE);
    public static final Supplier<Item> PIE_SLICE = registerPortion("pie_slice", PIE, 4, true);
    public static final Supplier<Item> CAKE = registerBlockMeal("cake", ModBlocks.CAKE, 1);
    public static final Supplier<Item> CAKE_SLICE = registerPortion("cake_slice", CAKE, 7, true);
    public static final Supplier<Item> MILKSHAKE = registerMeal("milkshake", Station.CRAFTING_TABLE, Items.GLASS_BOTTLE, UseAnim.DRINK);

    private static Item.Properties properties(String name) {
        //? if >=26.1 {
        /*return new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name)));
        *///?} else
        return new Item.Properties();
    }

    private static Item.Properties mealProperties(String name, @Nullable Item remainder, UseAnim animation) {
        Item.Properties properties = properties(name).stacksTo(MEAL_STACK_SIZE);
        if (remainder != null) {
            properties.craftRemainder(remainder);
            //? if >=26.1
            /*properties.usingConvertsTo(remainder);*/
        }
        //? if >=26.1
        /*properties.component(DataComponents.CONSUMABLE, animation == UseAnim.DRINK ? Consumables.DEFAULT_DRINK : Consumables.DEFAULT_FOOD);*/
        return properties;
    }

    private static Supplier<Item> registerMeal(String name, Station station, @Nullable Item remainder, UseAnim animation) {
        return registerWhole(name, station, () -> new MealItem(mealProperties(name, remainder, animation), animation));
    }

    private static Supplier<Item> registerBlockMeal(String name, Supplier<? extends Block> block, int stackSize) {
        //? if >=26.1 {
        /*Function<Item.Properties, Item.Properties> prefix = Item.Properties::useItemDescriptionPrefix;
        *///?} else
        Function<Item.Properties, Item.Properties> prefix = Function.identity();
        return registerWhole(name, Station.CRAFTING_TABLE,
                () -> new MealBlockItem(block.get(), prefix.apply(properties(name).stacksTo(stackSize))));
    }

    private static Supplier<Item> registerWhole(String name, Station station, Supplier<Item> supplier) {
        Supplier<Item> registered = ITEMS.register(name, supplier);
        MEALS.add(registered);
        if (station.isAvailable()) {
            CREATIVE_TAB_ITEMS.add(registered);
        }
        return registered;
    }

    private static Supplier<Item> registerPortion(String name, Supplier<Item> whole, int count, boolean sliceOfWhole) {
        Supplier<Item> registered = ITEMS.register(name, () -> new MealItem(mealProperties(name, null, UseAnim.EAT), UseAnim.EAT));
        MEALS.add(registered);
        PORTIONS.put(registered, new Portion(whole, count, sliceOfWhole));
        return registered;
    }

    public record Portion(Supplier<Item> whole, int count, boolean sliceOfWhole) {
    }

    public static Optional<Portion> portionOf(Item item) {
        return PORTIONS.entrySet().stream()
                .filter(entry -> entry.getKey().get() == item)
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public static void init() {
    }
}
