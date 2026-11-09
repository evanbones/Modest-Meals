package com.evandev.modest_meals.attribute;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.platform.RegistrationProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class ModAttributes {
    private static final RegistrationProvider<Attribute> ATTRIBUTES = RegistrationProvider.get(Registries.ATTRIBUTE);

    public static final double MIN_STAMINA_CAPACITY = 2.0;
    public static final double MAX_STAMINA_CAPACITY = 200.0;

    public static final Holder<Attribute> STAMINA_CAPACITY = ATTRIBUTES.register(
            "stamina_capacity",
            () -> new RangedAttribute(
                    "attribute.name." + Constants.MOD_ID + ".stamina_capacity",
                    20.0, MIN_STAMINA_CAPACITY, MAX_STAMINA_CAPACITY
            ).setSyncable(true)
    ).holder();

    public static void init() {
    }
}
