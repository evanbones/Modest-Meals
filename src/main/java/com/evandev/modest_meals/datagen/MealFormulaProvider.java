package com.evandev.modest_meals.datagen;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.food.meal.MealFormula;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class MealFormulaProvider extends JsonDataProvider<MealFormula> {

    public MealFormulaProvider(PackOutput output) {
        super(output, "modest_meals/formula", MealFormula.FULL_CODEC, "Modest Meals cooking formula");
    }

    @Override
    protected void collect(Map<ResourceLocation, MealFormula> entries) {
        entries.put(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "default"), MealFormula.DEFAULT);
    }
}
