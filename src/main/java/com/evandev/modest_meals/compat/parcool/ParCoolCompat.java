package com.evandev.modest_meals.compat.parcool;

import com.evandev.modest_meals.api.StaminaApi;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.platform.Platform;
import com.evandev.modest_meals.stamina.PlayerStamina;
import com.evandev.modest_meals.stamina.StaminaHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public final class ParCoolCompat {
    public static final String MOD_ID = "parcool";
    public static final String STAMINA_TYPE = "modest_meals:stamina";

    private static final int SYNC_INTERVAL_TICKS = 5;
    private static final Map<Player, Float> CLIENT_CARRY = new WeakHashMap<>();
    private static final Map<Player, Float> SERVER_CARRY = new WeakHashMap<>();

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = Platform.isModLoaded(MOD_ID);
        }
        return loaded;
    }

    public static boolean isActive() {
        return isLoaded() && ModConfig.get().parcoolStamina && StaminaApi.isEnabled();
    }

    public static boolean usesStamina(Player player) {
        return isActive() && !player.isCreative() && !player.isSpectator() && !Platform.isFakePlayer(player);
    }

    public static boolean isExhausted(Player player) {
        return StaminaHelper.get(player).getData().isExhausted();
    }

    public static void spendOnce(ServerPlayer player, Move move) {
        float cost = move.cost();
        if (cost <= 0.0F || !usesStamina(player)) {
            return;
        }
        PlayerStamina stamina = StaminaHelper.get(player);
        if (stamina.getData().isExhausted()) {
            return;
        }
        stamina.spend(cost);
        stamina.getData().setCooldown(Math.max(stamina.getData().getCooldown(), stamina.getCooldownInTicks()));
        stamina.syncNow();
    }

    public static void tick(Player player, PlayerStamina stamina) {
        if (!isLoaded() || player.level().isClientSide() && !player.isLocalPlayer()) {
            return;
        }
        Map<Player, Float> carries = player.level().isClientSide() ? CLIENT_CARRY : SERVER_CARRY;
        //? if parcool {
        float perSecond = usesStamina(player) ? ParCoolBridge.drainPerSecond(player) : 0.0F;
        //?} else
        //float perSecond = 0.0F;
        if (perSecond <= 0.0F || stamina.getData().isExhausted()) {
            carries.remove(player);
            return;
        }
        int maxLevel = Math.max(1, stamina.getMaxLevel());
        float carry = carries.getOrDefault(player, 0.0F) + perSecond / 20.0F * stamina.getActiveBarInTicks() / maxLevel;
        int ticks = (int) carry;
        carries.put(player, carry - ticks);
        stamina.spendTicks(ticks);
        stamina.getData().setCooldown(Math.max(stamina.getData().getCooldown(), stamina.getCooldownInTicks()));
        if (player instanceof ServerPlayer && player.tickCount % SYNC_INTERVAL_TICKS == 0) {
            stamina.syncNow();
        }
    }

    public enum Move {
        VAULT("vault", Charge.START, c -> c.parcoolVaultCost),
        WALL_JUMP("wall_jump", Charge.START, c -> c.parcoolWallJumpCost),
        WALL_RUN("wall_run", Charge.START, c -> c.parcoolWallRunCost),
        LONG_JUMP("long_jump", Charge.START, c -> c.parcoolLongJumpCost),
        CHARGE_JUMP("charge_jump", Charge.FINISH, c -> c.parcoolChargeJumpCost),
        TRICK_JUMP("trick_jump", Charge.START, c -> c.parcoolTrickJumpCost),
        DIVE("dive", Charge.START, c -> c.parcoolDiveCost),
        SLIDE("slide", Charge.START, c -> c.parcoolSlideCost),
        CLIMB_UP("climb_up", Charge.START, c -> c.parcoolClimbUpCost),
        CASTAWAY("castaway", Charge.START, c -> c.parcoolCastawayCost),
        DODGE("dodge", Charge.START, c -> c.parcoolDodgeCost),
        BREAKFALL("breakfall", Charge.START, c -> c.parcoolBreakfallCost),
        HORIZONTAL_WALL_RUN("horizontal_wall_run", Charge.PER_SECOND, c -> c.parcoolHorizontalWallRunCost),
        HANG_ON("hang_on", Charge.PER_SECOND, c -> c.parcoolHangOnCost),
        HANG_DOWN("hang_down", Charge.PER_SECOND, c -> c.parcoolHangDownCost),
        SLIDE_DOWN("slide_down", Charge.PER_SECOND, c -> c.parcoolSlideDownCost),
        POLE_CLIMB("pole_climb", Charge.PER_SECOND, c -> c.parcoolPoleClimbCost),
        CRAWL("crawl", Charge.PER_SECOND, c -> c.parcoolCrawlCost),
        SKYDIVE("skydive", Charge.PER_SECOND, c -> c.parcoolSkydiveCost),
        FAST_RUN("fast_run", Charge.PER_SECOND, c -> c.parcoolFastRunCost),
        FAST_SWIM("fast_swim", Charge.PER_SECOND, c -> c.parcoolFastSwimCost),
        RIDE_ZIPLINE("ride_zipline", Charge.PER_SECOND, c -> c.parcoolRideZiplineCost);

        private static final Map<String, Move> BY_ID = new HashMap<>();

        static {
            for (Move move : values()) {
                BY_ID.put(move.id, move);
            }
        }

        private final String id;
        private final Charge charge;
        private final Function<ModConfig, Float> cost;

        Move(String id, Charge charge, Function<ModConfig, Float> cost) {
            this.id = id;
            this.charge = charge;
            this.cost = cost;
        }

        public static Move byId(String id) {
            return BY_ID.get(id);
        }

        public Charge charge() {
            return this.charge;
        }

        public float cost() {
            return this.cost.apply(ModConfig.get());
        }
    }

    public enum Charge {
        START,
        FINISH,
        PER_SECOND
    }
}
