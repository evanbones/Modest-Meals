package com.evandev.modest_meals.effect;

import com.evandev.modest_meals.Constants;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ThermalResistanceEffect extends MobEffect {

    private final ResourceLocation directId;
    private final ResourceLocation environmentId;
    private final Supplier<Optional<Holder<Attribute>>> direct;
    private final Supplier<Optional<Holder<Attribute>>> environment;
    private final double directPerLevel;
    private final double environmentPerLevel;

    public ThermalResistanceEffect(String name,
                                   Supplier<Optional<Holder<Attribute>>> direct,
                                   Supplier<Optional<Holder<Attribute>>> environment,
                                   double directPerLevel,
                                   double environmentPerLevel,
                                   int color) {
        super(MobEffectCategory.BENEFICIAL, color);
        this.directId = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "effect." + name);
        this.environmentId = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "effect." + name + ".environment");
        this.direct = direct;
        this.environment = environment;
        this.directPerLevel = directPerLevel;
        this.environmentPerLevel = environmentPerLevel;
    }

    private static AttributeModifier modifier(ResourceLocation id, double amount) {
        return new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }

    @Override
    public void createModifiers(int amplifier, BiConsumer<Holder<Attribute>, AttributeModifier> output) {
        for (Entry entry : entries(amplifier)) {
            output.accept(entry.attribute(), entry.modifier());
        }
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributeMap, int amplifier) {
        for (Entry entry : entries(amplifier)) {
            AttributeInstance instance = attributeMap.getInstance(entry.attribute());
            if (instance != null) {
                instance.removeModifier(entry.modifier().id());
                instance.addPermanentModifier(entry.modifier());
            }
        }
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributeMap) {
        for (Entry entry : entries(0)) {
            AttributeInstance instance = attributeMap.getInstance(entry.attribute());
            if (instance != null) {
                instance.removeModifier(entry.modifier().id());
            }
        }
    }

    private List<Entry> entries(int amplifier) {
        List<Entry> entries = new ArrayList<>(2);
        int level = amplifier + 1;
        direct.get().ifPresent(attribute -> entries.add(
                new Entry(attribute, modifier(directId, directPerLevel * level))));
        environment.get().ifPresent(attribute -> entries.add(
                new Entry(attribute, modifier(environmentId, environmentPerLevel * level))));
        return entries;
    }

    private record Entry(Holder<Attribute> attribute, AttributeModifier modifier) {
    }
}
