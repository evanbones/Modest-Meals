package com.evandev.modest_meals.api;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

//? if neoforge {
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

    @ApiStatus.Internal
    public static boolean postDrain(Player player) {
        return NeoForge.EVENT_BUS.post(new Drain(player)).isCanceled();
    }

    @ApiStatus.Internal
    public static void postExhausted(Player player) {
        NeoForge.EVENT_BUS.post(new Exhausted(player));
    }

    @ApiStatus.Internal
    public static void postRecovered(Player player) {
        NeoForge.EVENT_BUS.post(new Recovered(player));
    }

    @ApiStatus.Internal
    public static void postRestored(Player player, float amount) {
        NeoForge.EVENT_BUS.post(new Restored(player, amount));
    }

    /**
     * Fired before sprinting takes stamina off a player, and cancellable to stop it.
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
//?} else {
/*import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/^*
 * Events fired as a player's stamina changes.
 *
 * <p>All of these are only ever invoked on the server. Read current values through {@link StaminaApi}.
 ^/
public final class StaminaEvent {
    /^*
     * Fired before sprinting takes stamina off a player. Return {@code false} to stop it.
     ^/
    public static final Event<Drain> DRAIN = EventFactory.createArrayBacked(Drain.class, listeners -> player -> {
        for (Drain listener : listeners) {
            if (!listener.allowDrain(player)) {
                return false;
            }
        }
        return true;
    });

    /^*
     * Fired when a player's stamina bar empties and they become exhausted.
     ^/
    public static final Event<PlayerCallback> EXHAUSTED = EventFactory.createArrayBacked(PlayerCallback.class,
            listeners -> player -> {
                for (PlayerCallback listener : listeners) {
                    listener.onEvent(player);
                }
            });

    /^*
     * Fired when a player finishes recharging and stops being exhausted.
     ^/
    public static final Event<PlayerCallback> RECOVERED = EventFactory.createArrayBacked(PlayerCallback.class,
            listeners -> player -> {
                for (PlayerCallback listener : listeners) {
                    listener.onEvent(player);
                }
            });

    /^*
     * Fired after something gives a player stamina back, with the amount granted in half-bolts.
     ^/
    public static final Event<Restored> RESTORED = EventFactory.createArrayBacked(Restored.class,
            listeners -> (player, amount) -> {
                for (Restored listener : listeners) {
                    listener.onRestored(player, amount);
                }
            });

    private StaminaEvent() {
    }

    @ApiStatus.Internal
    public static boolean postDrain(Player player) {
        return !DRAIN.invoker().allowDrain(player);
    }

    @ApiStatus.Internal
    public static void postExhausted(Player player) {
        EXHAUSTED.invoker().onEvent(player);
    }

    @ApiStatus.Internal
    public static void postRecovered(Player player) {
        RECOVERED.invoker().onEvent(player);
    }

    @ApiStatus.Internal
    public static void postRestored(Player player, float amount) {
        RESTORED.invoker().onRestored(player, amount);
    }

    @FunctionalInterface
    public interface Drain {
        boolean allowDrain(Player player);
    }

    @FunctionalInterface
    public interface PlayerCallback {
        void onEvent(Player player);
    }

    @FunctionalInterface
    public interface Restored {
        void onRestored(Player player, float amount);
    }
}
*///?}
