package com.evandev.modest_meals.client.hud;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.config.HeartTextureOption;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.config.SprintingOption;
import com.evandev.modest_meals.regen.HealthRegenHelper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import java.awt.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class RestoredHeartsDrawHelper {
    public static final ResourceLocation WHITE_FULL_HEART_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Constants.MOD_ID, "white_full_heart_texture"
    );
    public static final ResourceLocation WHITE_HALF_HEART_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Constants.MOD_ID, "white_half_heart_texture"
    );
    public static final ResourceLocation WHITE_RIGHT_HALF_HEART_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Constants.MOD_ID, "white_right_half_heart_texture"
    );
    public static final ResourceLocation ORIGINAL_RIGHT_HALF_HEART_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Constants.MOD_ID, "original_right_half_heart_texture"
    );
    public static final ResourceLocation BLINKING_RIGHT_HALF_HEART_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Constants.MOD_ID, "blinking_right_half_heart_texture"
    );
    private final int playerHealth;
    private final int totalNutritionToDraw;
    private final int heldFoodNutrition;
    private final int consumedNutrition;
    private final RandomSource random;
    private final int sprintingHealthLimit;
    private final boolean highlightRegeneratedHearts;
    private final boolean highlightRestoredHearts;
    private final Gui.HeartType heartType;
    private final boolean isHardcore;
    private int currentHeart;
    private int absorption;
    private int currentY;
    private Function<Boolean, RenderedHeart> restoredHeartGetter;
    private Supplier<RenderedHeart> restoredRightHalfHeartGetter;
    private Function<Boolean, RenderedHeart> regeneratedHeartGetter;

    public RestoredHeartsDrawHelper(Player player, RandomSource random) {
        this.heartType = getHeartTypeForPlayer(player);
        this.isHardcore = player.level().getLevelData().isHardcore();
        this.playerHealth = Mth.ceil(player.getHealth());
        this.absorption = Mth.ceil(player.getAbsorptionAmount());

        this.currentHeart = Mth.ceil(player.getMaxHealth());
        this.random = random;
        this.sprintingHealthLimit = ModConfig.get().sprinting == SprintingOption.LIMITED_BY_HEALTH ? ModConfig.get().sprintingHealthLimit : 4;
        boolean disableHunger = ModConfig.get().disableHunger;

        this.highlightRegeneratedHearts = disableHunger && ModConfig.get().highlightRegeneratedHearts;
        if (this.highlightRegeneratedHearts) {
            Color regeneratedHeartsOverlayColor = ModConfig.get().regeneratedHeartsOverlayColor;
            Color regeneratingHeartColor = new Color(
                    regeneratedHeartsOverlayColor.getRed(),
                    regeneratedHeartsOverlayColor.getGreen(),
                    regeneratedHeartsOverlayColor.getBlue(),
                    (int) Math.floor(calculateRegeneratingHeartOpacity() * 255F)
            );
            HeartTextureOption textureOption = ModConfig.get().regeneratedHeartsTexture;
            switch (textureOption) {
                case SINGLE_COLOR -> regeneratedHeartGetter = isHalf -> getWhiteHeart(isHalf, regeneratingHeartColor);
                case ORIGINAL ->
                        regeneratedHeartGetter = isHalf -> getAtlasSpriteHeart(heartType, isHalf, false, regeneratingHeartColor);
                case BLINKING ->
                        regeneratedHeartGetter = isHalf -> getAtlasSpriteHeart(heartType, isHalf, true, regeneratingHeartColor);
            }
        }

        this.highlightRestoredHearts = disableHunger && ModConfig.get().highlightRestoredHearts;
        if (this.highlightRestoredHearts) {
            Color restoredHeartsOverlayColor = ModConfig.get().restoredHeartsOverlayColor;
            HeartTextureOption textureOption = ModConfig.get().restoredHeartsTexture;
            switch (textureOption) {
                case SINGLE_COLOR -> {
                    restoredHeartGetter = isHalf -> getWhiteHeart(isHalf, restoredHeartsOverlayColor);
                    restoredRightHalfHeartGetter = () -> new RenderedHeart(WHITE_RIGHT_HALF_HEART_TEXTURE, false, restoredHeartsOverlayColor);
                }
                case ORIGINAL -> {
                    restoredHeartGetter = isHalf -> getAtlasSpriteHeart(Gui.HeartType.NORMAL, isHalf, false, restoredHeartsOverlayColor);
                    restoredRightHalfHeartGetter = () -> new RenderedHeart(ORIGINAL_RIGHT_HALF_HEART_TEXTURE, false, restoredHeartsOverlayColor);
                }
                case BLINKING -> {
                    restoredHeartGetter = isHalf -> getAtlasSpriteHeart(Gui.HeartType.NORMAL, isHalf, true, restoredHeartsOverlayColor);
                    restoredRightHalfHeartGetter = () -> new RenderedHeart(BLINKING_RIGHT_HALF_HEART_TEXTURE, false, restoredHeartsOverlayColor);
                }
            }
        }

        this.consumedNutrition = disableHunger ? HealthRegenHelper.get(player).getConsumedNutrition() : 0;
        this.heldFoodNutrition = disableHunger ? Mth.ceil(PreviewFood.healthPoints(player)) : 0;
        this.totalNutritionToDraw = this.highlightRegeneratedHearts ? (this.consumedNutrition + this.heldFoodNutrition) : this.heldFoodNutrition;
    }

    public static Gui.HeartType getHeartTypeForPlayer(Player player) {
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) {
            return Gui.HeartType.POISIONED;
        } else if (player.hasEffect(net.minecraft.world.effect.MobEffects.WITHER)) {
            return Gui.HeartType.WITHERED;
        } else if (player.isFullyFrozen()) {
            return Gui.HeartType.FROZEN;
        }
        return Gui.HeartType.NORMAL;
    }

    private RenderedHeart getWhiteHeart(boolean isHalf, Color color) {
        return new RenderedHeart(isHalf ? WHITE_HALF_HEART_TEXTURE : WHITE_FULL_HEART_TEXTURE, false, color);
    }

    private RenderedHeart getAtlasSpriteHeart(Gui.HeartType type, boolean isHalf, boolean isBlinking, Color color) {
        return new RenderedHeart(type.getSprite(isHardcore, isHalf, isBlinking), true, color);
    }

    private float calculateRegeneratingHeartOpacity() {
        float minOpacity = ModConfig.get().regeneratedHeartsOpacityMin;
        float amplitude = ModConfig.get().regeneratedHeartsOpacityMax - minOpacity;
        int period = Math.max(100, ModConfig.get().regeneratedHeartsBlinkingPeriod);
        return Mth.abs(
                Mth.sin(
                        (float) (Util.getMillis() % period) / (float) period * (float) (Math.PI * 2)
                ) * amplitude
        ) + minOpacity;
    }

    public Pair<RenderedHeart, RenderedHeart> heartsToDraw() {
        int heartsDiff = currentHeart - playerHealth - 1;
        if (heartsDiff < 0) {
            return new Pair<>(null, null);
        }
        if (highlightRegeneratedHearts && consumedNutrition > 0 && heartsDiff <= consumedNutrition) {
            // this heart is regenerating
            boolean isHalf = heartsDiff == consumedNutrition;
            RenderedHeart regeneratingHeart = regeneratedHeartGetter != null ? regeneratedHeartGetter.apply(isHalf) : null;
            if (isHalf && highlightRestoredHearts && heldFoodNutrition > 0 && restoredRightHalfHeartGetter != null) {
                // the left half of this heart is regenerating and the right half can be restored by held food
                return new Pair<>(regeneratingHeart, restoredRightHalfHeartGetter.get());
            }
            return new Pair<>(regeneratingHeart, null);
        }
        if (highlightRestoredHearts && totalNutritionToDraw > 0 && heartsDiff <= totalNutritionToDraw) {
            // this heart can be restored by held food
            boolean isHalf = heartsDiff == totalNutritionToDraw;
            return new Pair<>(restoredHeartGetter != null ? restoredHeartGetter.apply(isHalf) : null, null);
        }
        return new Pair<>(null, null);
    }

    public void updateCurrentHeart() {
        if (absorption <= 0) {
            currentHeart -= 2;
        } else {
            absorption -= 2;
        }
    }

    public int getCurrentY() {
        return currentY;
    }

    public int addShakingIfNeeded(int y) {
        if (playerHealth <= sprintingHealthLimit) {
            return y + this.random.nextInt(2);
        }
        return y;
    }

    public int updateCurrentY(int y) {
        y = addShakingIfNeeded(y);
        currentY = y;
        return y;
    }

    public record RenderedHeart(ResourceLocation texture, boolean isAtlasTexture, Color color) {
    }
}
