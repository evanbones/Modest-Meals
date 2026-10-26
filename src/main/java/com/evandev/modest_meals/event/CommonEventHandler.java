package com.evandev.modest_meals.event;

import com.evandev.modest_meals.api.StaminaApi;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.food.FoodProfileManager;
import com.evandev.modest_meals.food.ingredient.IngredientProfileManager;
import com.evandev.modest_meals.food.ingredient.MealEffectManager;
import com.evandev.modest_meals.food.ingredient.MealIngredientManager;
import com.evandev.modest_meals.food.meal.MealFormulaManager;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import com.evandev.modest_meals.network.ClientboundFoodDataSyncPayload;
import com.evandev.modest_meals.network.ClientboundStaminaSyncPayload;
import com.evandev.modest_meals.network.ModNetworking;
import com.evandev.modest_meals.regen.HealthRegenHelper;
import com.evandev.modest_meals.stamina.PlayerStamina;
import com.evandev.modest_meals.stamina.StaminaHelper;
import com.evandev.modest_meals.stamina.StaminaHolder;
import com.evandev.modest_meals.tag.ModBlockTags;
import com.evandev.modest_meals.trait.FoodTraitManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class CommonEventHandler {
    private static final float CRANK_STAMINA_COST = 0.13F;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        if (ModConfig.get().staminaSprint) {
            PlayerStamina stamina = StaminaHelper.get(player);
            stamina.tick();

            if (stamina.getData().isExhausted()) {
                if (player.isSprinting()) {
                    player.setSprinting(false);
                }
                if (player.isSwimming()) {
                    player.setSwimming(false);
                }
            }
        }

        if (!player.level().isClientSide()) {
            StaminaHelper.get(player).tickNaturalRegeneration();
            HealthRegenHelper.get(player).serverTick();
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!StaminaApi.isEnabled() || player.isCreative() || player.isSpectator() || player instanceof FakePlayer) {
            return;
        }
        if (!event.getLevel().getBlockState(event.getPos()).is(ModBlockTags.STAMINA_CRANKS)) {
            return;
        }
        if (StaminaApi.isExhausted(player)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
            return;
        }
        if (!player.level().isClientSide()) {
            PlayerStamina stamina = StaminaHelper.get(player);
            stamina.spend(CRANK_STAMINA_COST);
            stamina.getData().setCooldown(stamina.getCooldownInTicks());
            stamina.syncNow();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
            HealthRegenHelper.get(serverPlayer).sync();
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        StaminaHelper.remove(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();

        if (original instanceof StaminaHolder origHolder && player instanceof StaminaHolder newHolder) {
            if (!event.isWasDeath()) {
                newHolder.mm$setStaminaData(origHolder.mm$getStaminaData());
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
            HealthRegenHelper.get(serverPlayer).sync();
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerStamina stamina = StaminaHelper.get(serverPlayer);
            stamina.reset();
            HealthRegenHelper.get(serverPlayer).reset();
            ModNetworking.sendToPlayer(serverPlayer, ClientboundStaminaSyncPayload.create(serverPlayer));
            HealthRegenHelper.get(serverPlayer).sync();
        }
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(FoodTraitManager.INSTANCE);
        event.addListener(FoodProfileManager.INSTANCE);
        event.addListener(MealEffectManager.INSTANCE);
        event.addListener(IngredientProfileManager.INSTANCE);
        event.addListener(MealTypeManager.INSTANCE);
        event.addListener(MealFormulaManager.INSTANCE);
        event.addListener(MealIngredientManager.INSTANCE);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        ClientboundFoodDataSyncPayload payload = ClientboundFoodDataSyncPayload.create();
        event.getRelevantPlayers().forEach(player -> ModNetworking.sendToPlayer(player, payload));
    }

    @SubscribeEvent
    public static void onItemConsumed(LivingEntityUseItemEvent.Finish event) {
        FoodTraitManager.applyAll(event.getEntity(), event.getItem());
    }
}
