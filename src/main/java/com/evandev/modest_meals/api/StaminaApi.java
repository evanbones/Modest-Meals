package com.evandev.modest_meals.api;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.stamina.PlayerStamina;
import com.evandev.modest_meals.stamina.StaminaHelper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

/**
 * <p>Every method here that changes stamina syncs the result to the client. Call them on the server: on the client
 * they do nothing.
 *
 * <p>Amounts are in half-bolts, the same unit the bar is drawn in. A default bar holds {@code 20} half-bolts.
 *
 * <p>Example: restore a little stamina on a successful parry:
 * <pre>{@code
 * @SubscribeEvent
 * public static void onParry(ModParryEvent event) {
 *     StaminaApi.restore(event.getPlayer(), 2.0F);
 * }
 * }</pre>
 */
public final class StaminaApi {

    private StaminaApi() {
    }

    /**
     * Whether the stamina system is enabled at all.
     */
    public static boolean isEnabled() {
        return ModConfig.get().staminaSprint;
    }

    /**
     * The player's current stamina, in half-bolts.
     */
    public static int getStamina(Player player) {
        return stamina(player).getData().getStamina();
    }

    /**
     * How much stamina the player can hold, in half-bolts. Affected by the
     * {@code modest_meals:stamina_capacity} attribute.
     */
    public static int getMaxStamina(Player player) {
        return stamina(player).getMaxLevel();
    }

    /**
     * Whether the player has run out of stamina and is recharging.
     */
    public static boolean isExhausted(Player player) {
        return stamina(player).getData().isExhausted();
    }

    /**
     * Whether the bar has room for more stamina bolts.
     */
    public static boolean hasRoom(Player player) {
        return stamina(player).hasStaminaRoom();
    }

    /**
     * Give stamina back, up to the player's maximum. Fires {@link StaminaEvent.Restored}.
     *
     * @param amount half-bolts to restore, zero or less does nothing
     */
    public static void restore(Player player, float amount) {
        if (!canMutate(player, amount)) {
            return;
        }
        PlayerStamina stamina = stamina(player);
        stamina.addLevels(amount);
        stamina.syncNow();
        NeoForge.EVENT_BUS.post(new StaminaEvent.Restored(player, amount));
    }

    /**
     * Take stamina off the player. Emptying the bar exhausts them and fires {@link StaminaEvent.Exhausted}.
     *
     * @param amount half-bolts to spend, zero or less does nothing
     */
    public static void spend(Player player, float amount) {
        if (!canMutate(player, amount)) {
            return;
        }
        PlayerStamina stamina = stamina(player);
        stamina.spend(amount);
        stamina.syncNow();
    }

    /**
     * Reset the bar back to full and clear exhaustion.
     */
    public static void reset(Player player) {
        if (player.level().isClientSide() || !isEnabled()) {
            return;
        }
        PlayerStamina stamina = stamina(player);
        stamina.reset();
        stamina.syncNow();
    }

    private static boolean canMutate(Player player, float amount) {
        return amount > 0.0F && !player.level().isClientSide() && isEnabled();
    }

    private static PlayerStamina stamina(Player player) {
        return StaminaHelper.get(player);
    }
}
