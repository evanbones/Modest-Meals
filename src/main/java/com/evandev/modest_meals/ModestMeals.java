package com.evandev.modest_meals;

import com.evandev.modest_meals.attribute.ModAttributes;
import com.evandev.modest_meals.compat.combat_roll.CombatRollCompat;
import com.evandev.modest_meals.compat.cultural_delights.CulturalDelightsCompat;
import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightCompat;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.effect.ModMobEffects;
import com.evandev.modest_meals.recipe.ModRecipes;
import com.evandev.modest_meals.registry.ModBlocks;
import com.evandev.modest_meals.registry.ModCreativeTabs;
import com.evandev.modest_meals.registry.ModItems;
import com.evandev.modest_meals.trait.FoodTraitType;

public final class ModestMeals {
    public static void init() {
        ModAttributes.init();
        ModMobEffects.init();
        FoodTraitType.init();
        ModDataComponents.init();
        ModBlocks.init();
        ModItems.init();
        ModCreativeTabs.init();
        ModRecipes.init();
        FarmersDelightCompat.init();
        CulturalDelightsCompat.init();
        CombatRollCompat.init();
    }
}
