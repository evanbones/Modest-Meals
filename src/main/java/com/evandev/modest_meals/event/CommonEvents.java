package com.evandev.modest_meals.event;

import com.evandev.modest_meals.platform.Platform;
import com.evandev.modest_meals.api.StaminaApi;
import com.evandev.modest_meals.compat.reliable_gliders.ReliableGlidersCompat;
import com.evandev.modest_meals.compat.shield_expansion.ShieldExpansionCompat;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.food.FoodProfileManager;
import com.evandev.modest_meals.food.ingredient.IngredientProfileManager;
import com.evandev.modest_meals.food.ingredient.MealEffectManager;
import com.evandev.modest_meals.food.ingredient.MealIngredientManager;
import com.evandev.modest_meals.food.meal.MealFormulaManager;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import com.evandev.modest_meals.network.ClientboundConfigSyncPayload;
import com.evandev.modest_meals.network.ClientboundFoodDataSyncPayload;
import com.evandev.modest_meals.network.ClientboundStaminaSyncPayload;
import com.evandev.modest_meals.network.ModNetworking;
import com.evandev.modest_meals.regen.HealthRegenHelper;
import com.evandev.modest_meals.stamina.PlayerStamina;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.evandev.modest_meals.stamina.StaminaHolder;
import com.evandev.modest_meals.tag.ModBlockTags;
import com.evandev.modest_meals.trait.FoodTraitManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

public class CommonEvents {
    public static void onPlayerTick(Player player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        if (ModConfig.get().staminaSprint) {
            PlayerStamina stamina = StaminaHelper.get(player);
            stamina.tick();
            stamina.holdEnduranceWhileNourished();

            if (stamina.getData().isExhausted()) {
                if (ModConfig.get().staminaSprinting && !player.isPassenger()) {
                    if (player.isSprinting()) {
                        player.setSprinting(false);
                    }
                    if (player.isSwimming()) {
                        player.setSwimming(false);
                    }
                }
                if (ShieldExpansionCompat.isBlocking(player)) {
                    player.stopUsingItem();
                }
                if (stamina.isElytraGliding()) {
                    player.stopFallFlying();
                }
            }
        }

        if (player instanceof ServerPlayer serverPlayer && ReliableGlidersCompat.isLoaded()) {
            ReliableGlidersCompat.tickExhaustedFall(serverPlayer);
        }

        if (!player.level().isClientSide()) {
            StaminaHelper.get(player).tickNaturalRegeneration();
            HealthRegenHelper.get(player).serverTick();
        }
    }

    public static boolean allowBlockInteraction(Player player, Level level, BlockPos pos) {
        float cost = ModConfig.get().staminaHandCrankCost;
        if (cost <= 0.0F || !StaminaApi.isEnabled() || player.isCreative() || player.isSpectator() || Platform.isFakePlayer(player)) {
            return true;
        }
        if (!level.getBlockState(pos).is(ModBlockTags.STAMINA_CRANKS)) {
            return true;
        }
        if (StaminaApi.isExhausted(player)) {
            return false;
        }
        if (!player.level().isClientSide()) {
            PlayerStamina stamina = StaminaHelper.get(player);
            stamina.spend(cost);
            stamina.getData().setCooldown(stamina.getCooldownInTicks());
            stamina.syncNow();
        }
        return true;
    }

    public static void onPlayerLoggedIn(ServerPlayer serverPlayer) {
        ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
        HealthRegenHelper.get(serverPlayer).sync();
    }

    public static void onPlayerLoggedOut(Player player) {
        StaminaHelper.remove(player);
    }

    public static void onPlayerClone(Player original, Player player, boolean wasDeath) {
        if (original instanceof StaminaHolder origHolder && player instanceof StaminaHolder newHolder) {
            if (!wasDeath) {
                newHolder.mm$setStaminaData(origHolder.mm$getStaminaData());
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
            HealthRegenHelper.get(serverPlayer).sync();
        }
    }

    public static void onPlayerRespawn(ServerPlayer serverPlayer) {
        PlayerStamina stamina = StaminaHelper.get(serverPlayer);
        stamina.reset();
        HealthRegenHelper.get(serverPlayer).reset();
        ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
        HealthRegenHelper.get(serverPlayer).sync();
    }

    public static Map<String, PreparableReloadListener> serverReloadListeners() {
        return Map.of(
                "food_traits", FoodTraitManager.INSTANCE,
                "food_profiles", FoodProfileManager.INSTANCE,
                "meal_effects", MealEffectManager.INSTANCE,
                "ingredient_profiles", IngredientProfileManager.INSTANCE,
                "meal_types", MealTypeManager.INSTANCE,
                "meal_formula", MealFormulaManager.INSTANCE,
                "meal_ingredients", MealIngredientManager.INSTANCE
        );
    }

    public static void onDataPackReloaded(MinecraftServer server) {
        if (server.isDedicatedServer()) {
            ModConfig.load();
        }
    }

    public static void onDatapackSync(List<ServerPlayer> players) {
        players.forEach(ClientboundConfigSyncPayload::sendTo);
        ClientboundFoodDataSyncPayload payload = ClientboundFoodDataSyncPayload.create();
        players.forEach(player -> ModNetworking.sendToPlayer(player, payload));
    }

    public static void onItemConsumed(LivingEntity entity, ItemStack stack) {
        FoodTraitManager.applyAll(entity, stack);
    }
}
