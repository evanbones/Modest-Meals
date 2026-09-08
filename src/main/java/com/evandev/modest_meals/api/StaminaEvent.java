package com.evandev.modest_meals.api;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Events fired as a player's stamina changes.
 *
 * <p>All of these are posted on the {@link NeoForge#EVENT_BUS} and only ever on the server.
 * Read current values through {@link StaminaApi}.
 */
public abstract class StaminaEvent extends PlayerEvent {

    protected StaminaEvent(Player player) {
        super(player);
    }

    /**
     * Fired before sprinting takes stamina off a player, and cancellable to stop it.
     *
     * <p>Cancelling means the player sprints for free this tick. Useful for effects like "does not tire while doing X".
     */
    public static class Drain extends StaminaEvent implements ICancellableEvent {
        public Drain(Player player) {
            super(player);
        }
    }

    /**
     * Fired when a player's stamina bar empties and they become exhausted.
     */
    public static class Exhausted extends StaminaEvent {
        public Exhausted(Player player) {
            super(player);
        }
    }

    /**
     * Fired when a player finishes recharging and stops being exhausted.
     */
    public static class Recovered extends StaminaEvent {
        public Recovered(Player player) {
            super(player);
        }
    }

    /**
     * Fired after something gives a player stamina back.
     * <p>
     * {@code amount} is how much stamina was granted, in half-bolts
     */
    public static class Restored extends StaminaEvent {
        private final float amount;

        public Restored(Player player, float amount) {
            super(player);
            this.amount = amount;
        }

        public float getAmount() {
            return amount;
        }
    }
}
