package com.evandev.modest_meals.client.gui.editor;

import com.evandev.modest_meals.trait.FoodTrait;
import com.evandev.modest_meals.trait.FoodTraitType;
import com.evandev.modest_meals.trait.impl.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TraitEditorRegistry {
    private static final Map<FoodTraitType<?>, Supplier<TraitEditor<?>>> REGISTRY = new HashMap<>();

    private static final int TICKS_PER_BUBBLE = 30;
    private static final double MAX_SECONDS = 3600.0;

    static {
        register(FoodTraitType.EFFECT_GRANT, EffectGrantEditor::new);
        register(FoodTraitType.HEALTH_ADDITION, HealthAdditionEditor::new);
        register(FoodTraitType.HEALTH_REGEN, HealthRegenEditor::new);
        register(FoodTraitType.STAMINA_ADDITION, StaminaAdditionEditor::new);
        register(FoodTraitType.STAMINA_REGEN, StaminaRegenEditor::new);
        register(FoodTraitType.STAMINA_CAPACITY, StaminaCapacityEditor::new);
        register(FoodTraitType.EFFECT_REMOVAL, EffectRemovalEditor::new);
        register(FoodTraitType.AIR_BUBBLES, AirBubblesEditor::new);
        register(FoodTraitType.FIRE_EXTINGUISH, FireExtinguishEditor::new);
        register(FoodTraitType.TELEPORT, TeleportEditor::new);
        register(FoodTraitType.HEALTH_DEPLETION, HealthDepletionEditor::new);
        register(FoodTraitType.STAMINA_DEPLETION, StaminaDepletionEditor::new);
        register(FoodTraitType.HEALTH_NO_REGEN, HealthNoRegenEditor::new);
        register(FoodTraitType.STAMINA_NO_REGEN, StaminaNoRegenEditor::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends FoodTrait> void register(FoodTraitType<T> type, Supplier<TraitEditor<T>> supplier) {
        REGISTRY.put(type, (Supplier) supplier);
    }

    @SuppressWarnings("unchecked")
    public static <T extends FoodTrait> TraitEditor<T> getEditor(FoodTraitType<T> type) {
        Supplier<TraitEditor<?>> supplier = REGISTRY.get(type);
        return supplier != null ? (TraitEditor<T>) supplier.get() : new FallbackTraitEditor<>();
    }

    private static int toTicks(double seconds) {
        return (int) Math.round(seconds * 20.0);
    }

    private static double toSeconds(int ticks) {
        return ticks / 20.0;
    }

    private abstract static class ValueDurationEditor {
        protected float value;
        protected double durationSeconds;

        ValueDurationEditor(float defaultValue, double defaultSeconds) {
            this.value = defaultValue;
            this.durationSeconds = defaultSeconds;
        }

        protected void declare(FormBuilder form, String valueKey, double valueMin, double valueMax) {
            form.decimal(valueKey, valueMin, valueMax, value, v -> value = (float) v);
            form.decimal("gui.modest_meals.field.duration", 0, MAX_SECONDS, durationSeconds, v -> durationSeconds = v);
        }
    }

    private static class FallbackTraitEditor<T extends FoodTrait> implements TraitEditor<T> {
        private T trait;

        @Override
        public void initFrom(T trait) {
            this.trait = trait;
        }

        @Override
        public void buildForm(FormBuilder form) {
            form.note("gui.modest_meals.field.no_options");
        }

        @Override
        public T createTrait() {
            return trait;
        }
    }

    public static class EffectGrantEditor implements TraitEditor<EffectGrantTrait> {
        private final List<String> effectIds = new ArrayList<>();
        private String effectId;
        private double durationSeconds = 30;
        private int level = 1;
        private boolean showParticles = true;
        private boolean ambient = false;

        public EffectGrantEditor() {
            for (Holder<MobEffect> holder : BuiltInRegistries.MOB_EFFECT.asHolderIdMap()) {
                holder.unwrapKey().ifPresent(key -> effectIds.add(key.location().toString()));
            }
            effectIds.sort(String::compareTo);
            this.effectId = effectIds.isEmpty() ? null : effectIds.getFirst();
        }

        private static Component labelFor(String id) {
            Holder<MobEffect> holder = resolve(id);
            return holder != null ? Component.translatable(holder.value().getDescriptionId()) : Component.literal(String.valueOf(id));
        }

        private static Holder<MobEffect> resolve(String id) {
            if (id == null) return null;
            ResourceLocation loc = ResourceLocation.tryParse(id);
            if (loc == null) return null;
            return BuiltInRegistries.MOB_EFFECT.getHolder(loc).orElse(null);
        }

        @Override
        public void initFrom(EffectGrantTrait trait) {
            this.durationSeconds = toSeconds(trait.duration());
            this.level = trait.amplifier() + 1;
            this.showParticles = trait.showParticles();
            this.ambient = trait.ambient();
            trait.effect().unwrapKey().ifPresent(key -> this.effectId = key.location().toString());
        }

        @Override
        public void buildForm(FormBuilder form) {
            form.choice("gui.modest_meals.field.effect", effectIds, EffectGrantEditor::labelFor, effectId, v -> effectId = v,
                    "gui.modest_meals.search.effects");
            form.decimal("gui.modest_meals.field.duration", 0, MAX_SECONDS, durationSeconds, v -> durationSeconds = v);
            form.integer("gui.modest_meals.field.level", 1, 255, level, v -> level = (int) v);
            form.toggle("gui.modest_meals.field.particles", showParticles, v -> showParticles = v);
            form.toggle("gui.modest_meals.field.ambient", ambient, v -> ambient = v);
        }

        @Override
        public EffectGrantTrait createTrait() {
            Holder<MobEffect> holder = resolve(effectId);
            if (holder == null) holder = MobEffects.MOVEMENT_SPEED;
            return new EffectGrantTrait(holder, toTicks(durationSeconds), level - 1, showParticles, ambient);
        }
    }

    public static class EffectRemovalEditor implements TraitEditor<EffectRemovalTrait> {
        private boolean clearAll = false;

        @Override
        public void initFrom(EffectRemovalTrait trait) {
            this.clearAll = trait.clearAll();
        }

        @Override
        public void buildForm(FormBuilder form) {
            form.toggle("gui.modest_meals.field.clear_all", clearAll, v -> clearAll = v);
        }

        @Override
        public EffectRemovalTrait createTrait() {
            return new EffectRemovalTrait(clearAll);
        }
    }

    public static class HealthAdditionEditor extends ValueDurationEditor implements TraitEditor<HealthAdditionTrait> {
        public HealthAdditionEditor() {
            super(2.0f, 5.0);
        }

        @Override
        public void initFrom(HealthAdditionTrait trait) {
            this.value = trait.value();
            this.durationSeconds = toSeconds(trait.duration());
        }

        @Override
        public void buildForm(FormBuilder form) {
            declare(form, "gui.modest_meals.field.health", 0, 1024);
        }

        @Override
        public HealthAdditionTrait createTrait() {
            return new HealthAdditionTrait(value, toTicks(durationSeconds));
        }
    }

    public static class HealthDepletionEditor extends ValueDurationEditor implements TraitEditor<HealthDepletionTrait> {
        public HealthDepletionEditor() {
            super(2.0f, 0.0);
        }

        @Override
        public void initFrom(HealthDepletionTrait trait) {
            this.value = trait.value();
            this.durationSeconds = toSeconds(trait.duration());
        }

        @Override
        public void buildForm(FormBuilder form) {
            declare(form, "gui.modest_meals.field.health_loss", 0, 1024);
        }

        @Override
        public HealthDepletionTrait createTrait() {
            return new HealthDepletionTrait(value, toTicks(durationSeconds));
        }
    }

    public static class HealthRegenEditor extends ValueDurationEditor implements TraitEditor<HealthRegenTrait> {
        public HealthRegenEditor() {
            super(2.0f, 30.0);
        }

        @Override
        public void initFrom(HealthRegenTrait trait) {
            this.value = trait.value();
            this.durationSeconds = toSeconds(trait.duration());
        }

        @Override
        public void buildForm(FormBuilder form) {
            declare(form, "gui.modest_meals.field.potency", 0, 255);
        }

        @Override
        public HealthRegenTrait createTrait() {
            return new HealthRegenTrait(value, toTicks(durationSeconds));
        }
    }

    public static class HealthNoRegenEditor implements TraitEditor<HealthNoRegenTrait> {
        private double durationSeconds = 30;

        @Override
        public void initFrom(HealthNoRegenTrait trait) {
            this.durationSeconds = toSeconds(trait.duration());
        }

        @Override
        public void buildForm(FormBuilder form) {
            form.decimal("gui.modest_meals.field.duration", 0, MAX_SECONDS, durationSeconds, v -> durationSeconds = v);
        }

        @Override
        public HealthNoRegenTrait createTrait() {
            return new HealthNoRegenTrait(toTicks(durationSeconds));
        }
    }

    public static class StaminaAdditionEditor implements TraitEditor<StaminaAdditionTrait> {
        private float value = 2.0f;

        @Override
        public void initFrom(StaminaAdditionTrait trait) {
            this.value = trait.value();
        }

        @Override
        public void buildForm(FormBuilder form) {
            form.decimal("gui.modest_meals.field.stamina", 0, 1024, value, v -> value = (float) v);
        }

        @Override
        public StaminaAdditionTrait createTrait() {
            return new StaminaAdditionTrait(value);
        }
    }

    public static class StaminaRegenEditor extends ValueDurationEditor implements TraitEditor<StaminaRegenTrait> {
        public StaminaRegenEditor() {
            super(2.0f, 30.0);
        }

        @Override
        public void initFrom(StaminaRegenTrait trait) {
            this.value = trait.value();
            this.durationSeconds = toSeconds(trait.duration());
        }

        @Override
        public void buildForm(FormBuilder form) {
            declare(form, "gui.modest_meals.field.potency", 0, 255);
        }

        @Override
        public StaminaRegenTrait createTrait() {
            return new StaminaRegenTrait(value, toTicks(durationSeconds));
        }
    }

    public static class StaminaDepletionEditor extends ValueDurationEditor implements TraitEditor<StaminaDepletionTrait> {
        public StaminaDepletionEditor() {
            super(2.0f, 10.0);
        }

        @Override
        public void initFrom(StaminaDepletionTrait trait) {
            this.value = trait.value();
            this.durationSeconds = toSeconds(trait.duration());
        }

        @Override
        public void buildForm(FormBuilder form) {
            declare(form, "gui.modest_meals.field.potency", 0, 255);
        }

        @Override
        public StaminaDepletionTrait createTrait() {
            return new StaminaDepletionTrait(value, toTicks(durationSeconds));
        }
    }

    public static class StaminaCapacityEditor extends ValueDurationEditor implements TraitEditor<StaminaCapacityTrait> {
        public StaminaCapacityEditor() {
            super(2.0f, 60.0);
        }

        @Override
        public void initFrom(StaminaCapacityTrait trait) {
            this.value = trait.value();
            this.durationSeconds = toSeconds(trait.duration());
        }

        @Override
        public void buildForm(FormBuilder form) {
            declare(form, "gui.modest_meals.field.capacity", 0, 1024);
        }

        @Override
        public StaminaCapacityTrait createTrait() {
            return new StaminaCapacityTrait(value, toTicks(durationSeconds));
        }
    }

    public static class StaminaNoRegenEditor implements TraitEditor<StaminaNoRegenTrait> {
        private double durationSeconds = 30;

        @Override
        public void initFrom(StaminaNoRegenTrait trait) {
            this.durationSeconds = toSeconds(trait.duration());
        }

        @Override
        public void buildForm(FormBuilder form) {
            form.decimal("gui.modest_meals.field.duration", 0, MAX_SECONDS, durationSeconds, v -> durationSeconds = v);
        }

        @Override
        public StaminaNoRegenTrait createTrait() {
            return new StaminaNoRegenTrait(toTicks(durationSeconds));
        }
    }

    public static class AirBubblesEditor implements TraitEditor<AirBubblesTrait> {
        private int bubbles = 2;

        @Override
        public void initFrom(AirBubblesTrait trait) {
            this.bubbles = Math.round((float) trait.value() / TICKS_PER_BUBBLE);
        }

        @Override
        public void buildForm(FormBuilder form) {
            form.integer("gui.modest_meals.field.bubbles", 0, 100, bubbles, v -> bubbles = (int) v);
        }

        @Override
        public AirBubblesTrait createTrait() {
            return new AirBubblesTrait(bubbles * TICKS_PER_BUBBLE);
        }
    }

    public static class TeleportEditor implements TraitEditor<TeleportTrait> {
        private float range = 16.0f;

        @Override
        public void initFrom(TeleportTrait trait) {
            this.range = trait.range();
        }

        @Override
        public void buildForm(FormBuilder form) {
            form.decimal("gui.modest_meals.field.range", 0, 512, range, v -> range = (float) v);
        }

        @Override
        public TeleportTrait createTrait() {
            return new TeleportTrait(range);
        }
    }

    public static class FireExtinguishEditor implements TraitEditor<FireExtinguishTrait> {
        @Override
        public void initFrom(FireExtinguishTrait trait) {
        }

        @Override
        public void buildForm(FormBuilder form) {
            form.note("gui.modest_meals.field.no_options");
        }

        @Override
        public FireExtinguishTrait createTrait() {
            return new FireExtinguishTrait();
        }
    }
}
