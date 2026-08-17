package com.evandev.modest_meals.attribute;

import com.evandev.modest_meals.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Constants.MOD_ID);

    public static final double MIN_STAMINA_CAPACITY = 2.0;
    public static final double MAX_STAMINA_CAPACITY = 200.0;

    public static final DeferredHolder<Attribute, Attribute> STAMINA_CAPACITY = ATTRIBUTES.register(
            "stamina_capacity",
            () -> new RangedAttribute(
                    "attribute.name." + Constants.MOD_ID + ".stamina_capacity",
                    20.0, MIN_STAMINA_CAPACITY, MAX_STAMINA_CAPACITY
            ).setSyncable(true)
    );

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
        modBus.register(ModAttributes.class);
    }

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, STAMINA_CAPACITY);
    }
}
