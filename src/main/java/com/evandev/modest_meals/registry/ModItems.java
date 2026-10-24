package com.evandev.modest_meals.registry;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.meal.MealType.Station;
import com.evandev.modest_meals.item.MealItem;
import com.evandev.modest_meals.item.PizzaItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Constants.MOD_ID);

    public static final Set<Supplier<Item>> MEALS = new LinkedHashSet<>();
    public static final Set<Supplier<Item>> CREATIVE_TAB_ITEMS = new LinkedHashSet<>();
    public static final Map<Supplier<Item>, Portion> PORTIONS = new LinkedHashMap<>();

    public static final Supplier<Item> SOUP = registerBowlMeal("soup", Station.COOKING_POT);
    public static final Supplier<Item> RAMEN = registerBowlMeal("ramen", Station.COOKING_POT);
    public static final Supplier<Item> PASTA = registerBowlMeal("pasta", Station.COOKING_POT);
    public static final Supplier<Item> SALAD = registerBowlMeal("salad", Station.CRAFTING_TABLE);
    public static final Supplier<Item> CURRY = registerBowlMeal("curry", Station.COOKING_POT);
    public static final Supplier<Item> PORRIDGE = registerBowlMeal("porridge", Station.COOKING_POT);
    public static final Supplier<Item> DRINK = registerMeal("drink", Station.COOKING_POT,
            () -> new MealItem(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16),
                    UseAnim.DRINK));
    public static final Supplier<Item> JAM = registerMeal("jam", Station.COOKING_POT,
            () -> new MealItem(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16)));

    public static final Supplier<Item> SANDWICH = registerMeal("sandwich", Station.CRAFTING_TABLE);
    public static final Supplier<Item> WRAP = registerMeal("wrap", Station.CRAFTING_TABLE);
    public static final Supplier<Item> PIZZA = registerMeal("pizza", Station.CRAFTING_TABLE,
            () -> new PizzaItem(ModBlocks.PIZZA.get(), new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> PIZZA_SLICE = registerPortion("pizza_slice", PIZZA, 4);
    public static final Supplier<Item> ROLL = registerMeal("roll", Station.CRAFTING_TABLE);
    public static final Supplier<Item> ROLL_SLICE = registerPortion("roll_slice", ROLL, 3);
    public static final Supplier<Item> ICE_CREAM = registerBowlMeal("ice_cream", Station.VAT);
    // public static final Supplier<Item> PIE = registerMeal("pie");
    // public static final Supplier<Item> CAKE = registerMeal("cake");

    private static Supplier<Item> registerBowlMeal(String name, Station station) {
        return registerMeal(name, station,
                () -> new MealItem(new Item.Properties().craftRemainder(Items.BOWL).stacksTo(16)));
    }

    private static Supplier<Item> registerMeal(String name, Station station) {
        return registerMeal(name, station, () -> new MealItem(new Item.Properties().stacksTo(16)));
    }

    private static Supplier<Item> registerMeal(String name, Station station, Supplier<Item> supplier) {
        Supplier<Item> registered = ITEMS.register(name, supplier);
        MEALS.add(registered);
        if (station.isAvailable()) {
            CREATIVE_TAB_ITEMS.add(registered);
        }
        return registered;
    }

    /**
     * A share of a whole meal, made by cutting it up. It carries {@code 1 / count} of the whole meal.
     */
    private static Supplier<Item> registerPortion(String name, Supplier<Item> whole, int count) {
        Supplier<Item> registered = ITEMS.register(name, () -> new MealItem(new Item.Properties().stacksTo(16)));
        MEALS.add(registered);
        PORTIONS.put(registered, new Portion(whole, count));
        return registered;
    }

    public record Portion(Supplier<Item> whole, int count) {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
