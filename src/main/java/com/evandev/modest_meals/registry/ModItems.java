package com.evandev.modest_meals.registry;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.meal.MealType.Station;
import com.evandev.modest_meals.item.MealItem;
import com.evandev.modest_meals.item.MealBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
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
    public static final Supplier<Item> JAM = registerMeal("jam", Station.COOKING_POT,
            () -> new MealItem(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16)));

    public static final Supplier<Item> SANDWICH = registerMeal("sandwich", Station.CRAFTING_TABLE);
    public static final Supplier<Item> WRAP = registerMeal("wrap", Station.CRAFTING_TABLE);

    public static final Supplier<Item> PIZZA = registerMeal("pizza", Station.CRAFTING_TABLE,
            () -> new MealBlockItem(ModBlocks.PIZZA.get(), new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> PIZZA_SLICE = registerPortion("pizza_slice", PIZZA, 4, true);
    public static final Supplier<Item> ROLL = registerMeal("roll", Station.CRAFTING_TABLE);
    public static final Supplier<Item> ROLL_SLICE = registerPortion("roll_slice", ROLL, 3, false);
    public static final Supplier<Item> ICE_CREAM = registerMeal("ice_cream", Station.VAT);
    public static final Supplier<Item> ICE_CREAM_BOWL = registerBowlMeal("ice_cream_bowl", Station.VAT);
    public static final Supplier<Item> PIE = registerMeal("pie", Station.CRAFTING_TABLE,
            () -> new MealBlockItem(ModBlocks.PIE.get(), new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> PIE_SLICE = registerPortion("pie_slice", PIE, 4, true);
    public static final Supplier<Item> CAKE = registerMeal("cake", Station.CRAFTING_TABLE,
            () -> new MealBlockItem(ModBlocks.CAKE.get(), new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> CAKE_SLICE = registerPortion("cake_slice", CAKE, 7, true);
    public static final Supplier<Item> MILKSHAKE = registerMeal("milkshake", Station.CRAFTING_TABLE,
            () -> new MealItem(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16), UseAnim.DRINK));

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

    private static Supplier<Item> registerPortion(String name, Supplier<Item> whole, int count, boolean sliceOfWhole) {
        Supplier<Item> registered = ITEMS.register(name, () -> new MealItem(new Item.Properties().stacksTo(16)));
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

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
