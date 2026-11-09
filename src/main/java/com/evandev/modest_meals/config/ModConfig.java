package com.evandev.modest_meals.config;

import com.evandev.modest_meals.platform.Platform;
import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.network.ClientboundConfigSyncPayload;
import com.evandev.modest_meals.stamina.Corner;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.evandev.modest_meals.stamina.StaminaRegain;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class ModConfig {
    private static final TypeAdapter<Color> COLOR_ADAPTER = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Color value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.getRGB());
            }
        }

        @Override
        public Color read(JsonReader in) throws IOException {
            return new Color(in.nextInt(), true);
        }
    };
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Color.class, COLOR_ADAPTER)
            .create();
    private static final File CONFIG_FILE = Platform.getConfigDirectory().resolve(Constants.MOD_ID + ".json").toFile();
    private static final Set<String> SERVER_SYNCED_NAMES = serverSyncedNames();
    private static ModConfig INSTANCE;
    private static ModConfig effective;
    @Nullable
    private static JsonObject serverValues;
    @Nullable
    private static JsonObject lastSyncedValues;

    @ServerSynced
    @SerializedName("stamina_sprint")
    public boolean staminaSprint = true;

    @ServerSynced
    @SerializedName("stamina_infinite_peaceful")
    public boolean staminaInfinitePeaceful = false;

    @ServerSynced
    @SerializedName("stamina_regain_when_moving")
    public StaminaRegain staminaRegainWhenMoving = StaminaRegain.NORMAL;

    @ServerSynced
    @SerializedName("stamina_duration")
    public int staminaDuration = 33;

    @ServerSynced
    @SerializedName("stamina_recharge")
    public int staminaRecharge = 33;

    @ServerSynced
    @SerializedName("stamina_cooldown")
    public int staminaCooldown = 2;

    @ServerSynced
    @SerializedName("stamina_exhaustion_lockout")
    public boolean staminaExhaustionLockout = true;

    @ServerSynced
    @SerializedName("stamina_sprinting")
    public boolean staminaSprinting = true;

    @ServerSynced
    @SerializedName("stamina_gliding")
    public boolean staminaGliding = true;

    @ServerSynced
    @SerializedName("stamina_elytra")
    public boolean staminaElytra = false;

    @ServerSynced
    @SerializedName("stamina_shield_blocking")
    public boolean staminaShieldBlocking = true;

    @ServerSynced
    @SerializedName("stamina_parry_restore")
    public float staminaParryRestore = 3.0f;

    @ServerSynced
    @SerializedName("stamina_combat_roll_cost")
    public float staminaCombatRollCost = 4.0f;

    @ServerSynced
    @SerializedName("stamina_hand_crank_cost")
    public float staminaHandCrankCost = 0.13f;

    @ServerSynced
    @SerializedName("disable_hunger")
    public boolean disableHunger = true;

    @ServerSynced
    @SerializedName("hunger_effect")
    public HungerEffectOption hungerEffect = HungerEffectOption.VANILLA;

    @ServerSynced
    @SerializedName("hunger_replacement_effect")
    public String hungerReplacementEffect = "minecraft:poison";

    @ServerSynced
    @SerializedName("hunger_replacement_duration_multiplier")
    public float hungerReplacementDurationMultiplier = 0.5f;

    @ServerSynced
    @SerializedName("instant_eating")
    public boolean instantEating = false;

    @ServerSynced
    @SerializedName("always_eat")
    public boolean alwaysEat = false;

    @ServerSynced
    @SerializedName("gradual_health_regeneration")
    public boolean gradualHealthRegeneration = true;

    @ServerSynced
    @SerializedName("gradual_health_regeneration_speed")
    public float gradualHealthRegenerationSpeed = 1.0f;

    @ServerSynced
    @SerializedName("regeneration_at_full_health")
    public RegenerationAtFullHealthOption regenerationAtFullHealth = RegenerationAtFullHealthOption.CONTINUED;

    @ServerSynced
    @SerializedName("stamina_natural_regeneration")
    public boolean staminaNaturalRegeneration = false;

    @ServerSynced
    @SerializedName("stamina_natural_regeneration_fast_interval")
    public int staminaNaturalRegenerationFastInterval = 10;

    @ServerSynced
    @SerializedName("stamina_natural_regeneration_interval")
    public int staminaNaturalRegenerationInterval = 80;

    @ServerSynced
    @SerializedName("stamina_natural_regeneration_threshold")
    public int staminaNaturalRegenerationThreshold = 18;

    @ServerSynced
    @SerializedName("stamina_natural_regeneration_drain")
    public float staminaNaturalRegenerationDrain = 1.5f;

    @ServerSynced
    @SerializedName("sprinting")
    public SprintingOption sprinting = SprintingOption.VANILLA;

    @ServerSynced
    @SerializedName("sprinting_health_limit")
    public int sprintingHealthLimit = 6;

    @SerializedName("show_food_item_tooltips")
    public boolean showFoodItemTooltips = true;

    @SerializedName("show_food_regeneration_tooltips")
    public boolean showFoodRegenerationTooltips = false;

    @SerializedName("show_food_trait_tooltips")
    public boolean showFoodTraitTooltips = true;

    @SerializedName("when_eaten_tooltip")
    public TooltipVisibility whenEatenTooltip = TooltipVisibility.VISIBLE;

    @SerializedName("ingredient_tooltip")
    public TooltipVisibility ingredientTooltip = TooltipVisibility.VISIBLE;

    @ServerSynced
    @SerializedName("food_effects")
    public boolean foodEffects = true;

    @ServerSynced
    @SerializedName("trait_global_value_multiplier")
    public float traitGlobalValueMultiplier = 1.0f;

    @ServerSynced
    @SerializedName("trait_global_duration_multiplier")
    public float traitGlobalDurationMultiplier = 1.0f;

    @SerializedName("hud_layout")
    public HudLayoutOption hudLayout = HudLayoutOption.DEFAULT;

    @SerializedName("hide_hunger_bar")
    public boolean hideHungerBar = true;

    @SerializedName("hide_stamina_bar")
    public boolean hideStaminaBar = false;

    @SerializedName("stamina_bar_offset_x")
    public int staminaBarOffsetX = 0;

    @SerializedName("stamina_bar_offset_y")
    public int staminaBarOffsetY = 0;

    @SerializedName("hide_stamina_bar_moving")
    public boolean hideStaminaBarMoving = false;

    @SerializedName("hide_stamina_bar_cooldown")
    public boolean hideStaminaBarCooldown = true;

    @SerializedName("hide_stamina_bar_inactive")
    public boolean hideStaminaBarInactive = false;

    @SerializedName("highlight_stamina_bar")
    public boolean highlightStaminaBar = false;

    @SerializedName("flash_stamina_bar_when_full")
    public boolean flashStaminaBarWhenFull = true;

    @SerializedName("flash_stamina_bar_at")
    public int flashStaminaBarAt = 0;

    @SerializedName("show_stamina_text")
    public boolean showStaminaText = false;

    @SerializedName("use_dynamic_stamina_color")
    public boolean useDynamicStaminaColor = true;

    @SerializedName("alt_stamina_show_on_active")
    public boolean altStaminaShowOnActive = false;

    @SerializedName("alt_stamina_corner")
    public Corner altStaminaCorner = Corner.TOP_LEFT;

    @SerializedName("alt_stamina_offset_x")
    public int altStaminaOffsetX = 0;

    @SerializedName("alt_stamina_offset_y")
    public int altStaminaOffsetY = 0;

    @SerializedName("alt_stamina_shadow")
    public boolean altStaminaShadow = true;

    @SerializedName("alt_stamina_text")
    public String altStaminaText = "Stamina: %v%";

    @SerializedName("highlight_restored_stamina")
    public boolean highlightRestoredStamina = true;

    @SerializedName("restored_stamina_overlay_color")
    public Color restoredStaminaOverlayColor = new Color(177, 155, 63);

    @SerializedName("highlight_restored_hearts")
    public boolean highlightRestoredHearts = true;

    @SerializedName("restored_hearts_texture")
    public HeartTextureOption restoredHeartsTexture = HeartTextureOption.BLINKING;

    @SerializedName("restored_hearts_overlay_color")
    public Color restoredHeartsOverlayColor = new Color(120, 0, 20);

    @SerializedName("highlight_regenerated_hearts")
    public boolean highlightRegeneratedHearts = true;

    @SerializedName("regenerated_hearts_texture")
    public HeartTextureOption regeneratedHeartsTexture = HeartTextureOption.BLINKING;

    @SerializedName("regenerated_hearts_overlay_color")
    public Color regeneratedHeartsOverlayColor = new Color(255, 135, 135);

    @SerializedName("regenerated_hearts_opacity_min")
    public float regeneratedHeartsOpacityMin = 0.1f;

    @SerializedName("regenerated_hearts_opacity_max")
    public float regeneratedHeartsOpacityMax = 1.0f;

    @SerializedName("regenerated_hearts_blinking_period")
    public int regeneratedHeartsBlinkingPeriod = 1500;

    public static ModConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return effective;
    }

    public static ModConfig local() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static boolean isServerControlled() {
        return serverValues != null;
    }

    public static boolean isServerControlled(String option) {
        return serverValues != null && SERVER_SYNCED_NAMES.contains(option);
    }

    public static JsonObject serverSyncedValues() {
        JsonObject all = GSON.toJsonTree(get()).getAsJsonObject();
        JsonObject synced = new JsonObject();
        for (String name : SERVER_SYNCED_NAMES) {
            if (all.has(name)) {
                synced.add(name, all.get(name));
            }
        }
        return synced;
    }

    public static void applyServerValues(@Nullable JsonObject values) {
        serverValues = values;
        refreshEffective();
    }

    private static void refreshEffective() {
        if (serverValues == null) {
            effective = INSTANCE;
            lastSyncedValues = serverSyncedValues();
            return;
        }
        JsonObject merged = GSON.toJsonTree(INSTANCE).getAsJsonObject();
        for (String name : SERVER_SYNCED_NAMES) {
            if (serverValues.has(name)) {
                merged.add(name, serverValues.get(name));
            }
        }
        ModConfig config = GSON.fromJson(merged, ModConfig.class);
        config.validateDefaults();
        effective = config;
        lastSyncedValues = serverSyncedValues();
    }

    private static Set<String> serverSyncedNames() {
        Set<String> names = new LinkedHashSet<>();
        for (Field field : ModConfig.class.getDeclaredFields()) {
            SerializedName name = field.getAnnotation(SerializedName.class);
            if (name != null && field.isAnnotationPresent(ServerSynced.class)) {
                names.add(name.value());
            }
        }
        return Set.copyOf(names);
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new ModConfig();
                }
                INSTANCE.validateDefaults();
                refreshEffective();
            } catch (Exception e) {
                Constants.LOG.error("Failed to load " + Constants.MOD_ID + ".json", e);
                INSTANCE = new ModConfig();
                save();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        JsonObject syncedBefore = lastSyncedValues;
        refreshEffective();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            Constants.LOG.error("Failed to save " + Constants.MOD_ID + ".json", e);
        }
        if (!lastSyncedValues.equals(syncedBefore)) {
            ClientboundConfigSyncPayload.broadcast();
            StaminaHelper.reset();
        }
    }

    public static Holder<MobEffect> getHungerReplacementEffect() {
        Optional<Holder.Reference<MobEffect>> effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(
                ResourceLocation.parse(get().hungerReplacementEffect)
        );
        return effectHolder.map(h -> (Holder<MobEffect>) h).orElse(MobEffects.POISON);
    }

    public void validateDefaults() {
        if (hudLayout == null) {
            hudLayout = HudLayoutOption.DEFAULT;
        }
        if (whenEatenTooltip == null) {
            whenEatenTooltip = TooltipVisibility.VISIBLE;
        }
        if (ingredientTooltip == null) {
            ingredientTooltip = TooltipVisibility.VISIBLE;
        }
        if (restoredHeartsOverlayColor == null) {
            restoredHeartsOverlayColor = new Color(120, 0, 20);
        }
        if (regeneratedHeartsOverlayColor == null) {
            regeneratedHeartsOverlayColor = new Color(255, 135, 135);
        }
        if (restoredStaminaOverlayColor == null) {
            restoredStaminaOverlayColor = new Color(90, 140, 60);
        }
    }
}
