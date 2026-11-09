package com.evandev.modest_meals.compat.shield_expansion;

import com.evandev.modest_meals.platform.Platform;
import com.evandev.modest_meals.api.StaminaApi;
import com.evandev.modest_meals.config.ModConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
//? if neoforge && shield_expansion {
/*import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
*///?}

public final class ShieldExpansionCompat {
    public static final String MOD_ID = "shieldexp";

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = Platform.isModLoaded(MOD_ID);
        }
        return loaded;
    }

    //? if neoforge && shield_expansion {
    /*public static void init() {
        if (!isLoaded()) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(ShieldExpansionCompat::onStartUsing);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ShieldExpansionCompat::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ShieldExpansionCompat::onProjectileImpact);
    }

    *///?}

    public static boolean isBlocking(Player player) {
        //? if shield_expansion {
        return ModConfig.get().staminaShieldBlocking && isLoaded() && ShieldExpansionBridge.isRaisingShield(player);
        //?} else
        /*return false;*/
    }

    private static boolean tracksStamina(Player player) {
        return StaminaApi.isEnabled() && !player.isCreative() && !player.isSpectator() && !Platform.isFakePlayer(player);
    }

    //? if neoforge && shield_expansion {
    /*private static void onStartUsing(LivingEntityUseItemEvent.Start event) {
        if (!ModConfig.get().staminaShieldBlocking || !(event.getEntity() instanceof Player player) || !tracksStamina(player)) {
            return;
        }
        if (ShieldExpansionBridge.isShield(event.getItem()) && StaminaApi.isExhausted(player)) {
            event.setCanceled(true);
        }
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide() || !tracksStamina(player)) {
            return;
        }
        if (ShieldExpansionBridge.willParry(player, event.getSource())) {
            onParry(player);
        }
    }

    private static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Player player)
                || player.level().isClientSide() || !tracksStamina(player)) {
            return;
        }
        if (ShieldExpansionBridge.willParry(player, event.getEntity())) {
            onParry(player);
        }
    }
    *///?}

    private static void onParry(Player player) {
        StaminaApi.restore(player, ModConfig.get().staminaParryRestore);
    }
}
