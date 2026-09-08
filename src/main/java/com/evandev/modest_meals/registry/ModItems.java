package com.evandev.modest_meals.registry;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.item.MealItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Constants.MOD_ID);

    public static final Set<Supplier<Item>> CREATIVE_TAB_ITEMS = new LinkedHashSet<>();

    public static final Supplier<Item> SOUP = registerBowlMeal("soup");
    public static final Supplier<Item> RAMEN = registerBowlMeal("ramen");
    public static final Supplier<Item> PASTA = registerBowlMeal("pasta");
    public static final Supplier<Item> SALAD = registerBowlMeal("salad");
    public static final Supplier<Item> CURRY = registerBowlMeal("curry");
    public static final Supplier<Item> PORRIDGE = registerBowlMeal("porridge");
    public static final Supplier<Item> DRINK = registerMeal("drink",
            () -> new MealItem(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16),
                    UseAnim.DRINK));
    public static final Supplier<Item> JAM = registerBottleMeal("jam");
    public static final Supplier<Item> PICKLES = registerBottleMeal("pickles");

    public static final Supplier<Item> SANDWICH = registerMeal("sandwich");
    public static final Supplier<Item> WRAP = registerMeal("wrap");
    public static final Supplier<Item> PIZZA = registerMeal("pizza");
    public static final Supplier<Item> SUSHI = registerMeal("sushi");
    public static final Supplier<Item> SKEWER = registerMeal("skewer");
    public static final Supplier<Item> MOCHI = registerMeal("mochi");
    public static final Supplier<Item> ICE_CREAM = registerMeal("ice_cream");
    public static final Supplier<Item> POPSICLE = registerMeal("popsicle");
    public static final Supplier<Item> PIE = registerMeal("pie");
    public static final Supplier<Item> CAKE = registerMeal("cake");

    private static Supplier<Item> registerBowlMeal(String name) {
        return registerMeal(name,
                () -> new MealItem(new Item.Properties().craftRemainder(Items.BOWL).stacksTo(16)));
    }

    private static Supplier<Item> registerBottleMeal(String name) {
        return registerMeal(name,
                () -> new MealItem(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16)));
    }

    private static Supplier<Item> registerMeal(String name) {
        return registerMeal(name, () -> new MealItem(new Item.Properties().stacksTo(16)));
    }

    private static Supplier<Item> registerMeal(String name, Supplier<Item> supplier) {
        Supplier<Item> registered = ITEMS.register(name, supplier);
        CREATIVE_TAB_ITEMS.add(registered);
        return registered;
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
