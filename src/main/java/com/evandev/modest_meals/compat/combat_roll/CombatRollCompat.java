package com.evandev.modest_meals.compat.combat_roll;

import com.evandev.modest_meals.platform.Platform;
import com.evandev.modest_meals.api.StaminaApi;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.stamina.PlayerStamina;
import com.evandev.modest_meals.stamina.StaminaHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class CombatRollCompat {
    public static final String MOD_ID = "combat_roll";

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = Platform.isModLoaded(MOD_ID);
        }
        return loaded;
    }

    public static void init() {
        if (isLoaded()) {
            CombatRollBridge.onRoll(CombatRollCompat::onRoll);
        }
    }

    public static boolean usesStamina(Player player) {
        return ModConfig.get().staminaCombatRollCost > 0.0F && StaminaApi.isEnabled()
                && !player.isCreative() && !player.isSpectator() && !Platform.isFakePlayer(player);
    }

    public static boolean canRoll(Player player) {
        return !usesStamina(player) || !StaminaHelper.get(player).getData().isExhausted();
    }

    private static void onRoll(ServerPlayer player) {
        if (!usesStamina(player)) {
            return;
        }
        PlayerStamina stamina = StaminaHelper.get(player);
        stamina.spend(ModConfig.get().staminaCombatRollCost);
        stamina.getData().setCooldown(stamina.getCooldownInTicks());
        stamina.syncNow();
    }
}
