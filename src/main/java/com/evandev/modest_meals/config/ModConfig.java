package com.evandev.modest_meals.config;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.stamina.Corner;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.evandev.modest_meals.stamina.StaminaRegain;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.fml.loading.FMLPaths;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

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
    private static final File CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve(Constants.MOD_ID + ".json").toFile();
    private static ModConfig INSTANCE;

    @SerializedName("stamina_sprint")
    public boolean staminaSprint = true;

    @SerializedName("stamina_infinite_peaceful")
    public boolean staminaInfinitePeaceful = false;

    @SerializedName("stamina_regain_when_moving")
    public StaminaRegain staminaRegainWhenMoving = StaminaRegain.NORMAL;

    @SerializedName("stamina_duration")
    public int staminaDuration = 33;

    @SerializedName("stamina_recharge")
    public int staminaRecharge = 33;

    @SerializedName("stamina_cooldown")
    public int staminaCooldown = 2;

    @SerializedName("disable_hunger")
    public boolean disableHunger = true;

    @SerializedName("hunger_effect")
    public HungerEffectOption hungerEffect = HungerEffectOption.VANILLA;

    @SerializedName("hunger_replacement_effect")
    public String hungerReplacementEffect = "minecraft:poison";

    @SerializedName("hunger_replacement_duration_multiplier")
    public float hungerReplacementDurationMultiplier = 0.5f;

    @SerializedName("instant_eating")
    public boolean instantEating = false;

    @SerializedName("gradual_health_regeneration")
    public boolean gradualHealthRegeneration = true;

    @SerializedName("gradual_health_regeneration_speed")
    public float gradualHealthRegenerationSpeed = 1.0f;

    @SerializedName("regeneration_at_full_health")
    public RegenerationAtFullHealthOption regenerationAtFullHealth = RegenerationAtFullHealthOption.CONTINUED;

    @SerializedName("sprinting")
    public SprintingOption sprinting = SprintingOption.VANILLA;

    @SerializedName("sprinting_health_limit")
    public int sprintingHealthLimit = 6;

    @SerializedName("show_food_item_tooltips")
    public boolean showFoodItemTooltips = true;

    @SerializedName("show_food_regeneration_tooltips")
    public boolean showFoodRegenerationTooltips = false;

    @SerializedName("show_food_trait_tooltips")
    public boolean showFoodTraitTooltips = true;

    @SerializedName("trait_global_value_multiplier")
    public float traitGlobalValueMultiplier = 1.0f;

    @SerializedName("trait_global_duration_multiplier")
    public float traitGlobalDurationMultiplier = 1.0f;

    @SerializedName("smelting_multiplier")
    public float smeltingMultiplier = 1.25f;

    @SerializedName("smelting_duration_multiplier")
    public float smeltingDurationMultiplier = 1.25f;

    @SerializedName("hide_hunger_bar")
    public boolean hideHungerBar = true;

    @SerializedName("hide_stamina_bar")
    public boolean hideStaminaBar = false;

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
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new ModConfig();
                }
                INSTANCE.validateDefaults();
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
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
            StaminaHelper.reset();
        } catch (IOException e) {
            Constants.LOG.error("Failed to save " + Constants.MOD_ID + ".json", e);
        }
    }

    public static Holder<MobEffect> getHungerReplacementEffect() {
        Optional<Holder.Reference<MobEffect>> effectHolder = BuiltInRegistries.MOB_EFFECT.getHolder(
                ResourceLocation.parse(get().hungerReplacementEffect)
        );
        return effectHolder.map(h -> (Holder<MobEffect>) h).orElse(MobEffects.POISON);
    }

    public void validateDefaults() {
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
