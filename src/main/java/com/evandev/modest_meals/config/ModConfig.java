package com.evandev.modest_meals.config;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.stamina.Corner;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.evandev.modest_meals.stamina.StaminaRegain;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.neoforged.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve(Constants.MOD_ID + ".json").toFile();
    private static ModConfig INSTANCE;

    @SerializedName("stamina_sprint")
    public boolean staminaSprint = true;

    @SerializedName("stamina_infinite_peaceful")
    public boolean staminaInfinitePeaceful = false;

    @SerializedName("stamina_regain_when_moving")
    public StaminaRegain staminaRegainWhenMoving = StaminaRegain.NORMAL;

    @SerializedName("stamina_duration")
    public int staminaDuration = 5;

    @SerializedName("stamina_recharge")
    public int staminaRecharge = 7;

    @SerializedName("stamina_cooldown")
    public int staminaCooldown = 1;

    @SerializedName("stamina_saturation_effect")
    public boolean staminaSaturationEffect = false;

    @SerializedName("stamina_hunger_effect")
    public boolean staminaHungerEffect = false;

    @SerializedName("hide_stamina_bar")
    public boolean hideStaminaBar = false;

    @SerializedName("hide_stamina_bar_moving")
    public boolean hideStaminaBarMoving = false;

    @SerializedName("hide_stamina_bar_cooldown")
    public boolean hideStaminaBarCooldown = false;

    @SerializedName("hide_stamina_bar_inactive")
    public boolean hideStaminaBarInactive = false;

    @SerializedName("highlight_stamina_bar")
    public boolean highlightStaminaBar = false;

    @SerializedName("flash_stamina_bar_when_full")
    public boolean flashStaminaBarWhenFull = true;

    @SerializedName("flash_stamina_bar_at")
    public int flashStaminaBarAt = 7;

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
}
