package com.evandev.modest_meals.compat.cold_sweat;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class MealResistanceTempModifier extends TempModifier {

    private static final String AMOUNT_KEY = "Amount";

    public MealResistanceTempModifier() {
    }

    public MealResistanceTempModifier(double amount) {
        setAmount(amount);
    }

    public double getAmount() {
        return getNBT().getDouble(AMOUNT_KEY);
    }

    public void setAmount(double amount) {
        getNBT().putDouble(AMOUNT_KEY, amount);
        markDirty();
    }

    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait) {
        double amount = getAmount();
        return value -> Math.min(1.0, value + amount);
    }
}
