package com.evandev.modest_meals.compat.parcool;

//? if parcool {
import com.alrex.parcool.api.ParCoolMobEffects;
import com.alrex.parcool.api.action.Action;
import com.alrex.parcool.api.action.ContinuableAction;
import com.alrex.parcool.api.action.ParCoolActionEvent;
import com.alrex.parcool.api.action.SynchronizedProperty;
import com.alrex.parcool.api.stamina.RegisterParCoolStaminaTypeEvent;
import com.alrex.parcool.api.stamina.StaminaTypeEntry;
import com.alrex.parcool.common.Parkourability;
import com.alrex.parcool.common.action.impl.ChargeJump;
import com.evandev.modest_meals.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public final class ParCoolBridge {
    private static final StaminaTypeEntry<ModestMealsStamina> STAMINA_TYPE = new StaminaTypeEntry<>(
            ResourceLocation.parse(ParCoolCompat.STAMINA_TYPE), "stamina", ModestMealsStamina::new);
    @Nullable
    private static final Field CHARGE_JUMP_TRIGGERED = chargeJumpTriggeredField();

    public static void register() {
        ModList.get().getModContainerById(ParCoolCompat.MOD_ID).map(ModContainer::getEventBus).ifPresent(bus ->
                bus.addListener(RegisterParCoolStaminaTypeEvent.class, event -> event.register(STAMINA_TYPE)));
        IEventBus gameBus = NeoForge.EVENT_BUS;
        gameBus.addListener(ParCoolActionEvent.TryToStart.class, ParCoolBridge::onTryToStart);
        gameBus.addListener(ParCoolActionEvent.TryToContinue.class, ParCoolBridge::onTryToContinue);
        gameBus.addListener(ParCoolActionEvent.Start.Post.class, ParCoolBridge::onStart);
        gameBus.addListener(ParCoolActionEvent.Finish.Post.class, ParCoolBridge::onFinish);
    }

    static float drainPerSecond(Player player) {
        Parkourability parkourability = Parkourability.get(player);
        if (parkourability == null || isInexhaustible(player)) {
            return 0.0F;
        }
        float total = 0.0F;
        for (Action action : parkourability.getActions()) {
            ParCoolCompat.Move move = moveOf(action);
            if (move != null && move.charge() == ParCoolCompat.Charge.PER_SECOND
                    && action instanceof ContinuableAction continuable && continuable.isDoing()) {
                total += move.cost();
            }
        }
        return total;
    }

    private static void onTryToStart(ParCoolActionEvent.TryToStart event) {
        ParCoolCompat.Move move = moveOf(event.getAction());
        if (move != null && isBlocked(event.getPlayer(), move)) {
            event.setCanceled(true);
        }
    }

    private static void onTryToContinue(ParCoolActionEvent.TryToContinue event) {
        ParCoolCompat.Move move = moveOf(event.getAction());
        if (move != null && move.charge() == ParCoolCompat.Charge.PER_SECOND && isBlocked(event.getPlayer(), move)) {
            event.setCanceled(true);
        }
    }

    private static void onStart(ParCoolActionEvent.Start.Post event) {
        ParCoolCompat.Move move = moveOf(event.getAction());
        if (move != null && move.charge() == ParCoolCompat.Charge.START
                && event.getPlayer() instanceof ServerPlayer player && !isInexhaustible(player)) {
            ParCoolCompat.spendOnce(player, move);
        }
    }

    private static void onFinish(ParCoolActionEvent.Finish.Post event) {
        ParCoolCompat.Move move = moveOf(event.getAction());
        if (move != null && move.charge() == ParCoolCompat.Charge.FINISH
                && event.getPlayer() instanceof ServerPlayer player && !isInexhaustible(player) && jumped(event.getAction())) {
            ParCoolCompat.spendOnce(player, move);
        }
    }

    private static boolean isBlocked(Player player, ParCoolCompat.Move move) {
        return move.cost() > 0.0F && ParCoolCompat.usesStamina(player) && !isInexhaustible(player)
                && ParCoolCompat.isExhausted(player);
    }

    private static boolean isInexhaustible(Player player) {
        return player.hasEffect(ParCoolMobEffects.INEXHAUSTIBLE);
    }

    @Nullable
    private static ParCoolCompat.Move moveOf(Action action) {
        ResourceLocation id = action.getEntry().id();
        return ParCoolCompat.MOD_ID.equals(id.getNamespace()) ? ParCoolCompat.Move.byId(id.getPath()) : null;
    }

    @SuppressWarnings("unchecked")
    private static boolean jumped(Action action) {
        if (!(action instanceof ChargeJump) || CHARGE_JUMP_TRIGGERED == null) {
            return true;
        }
        try {
            return ((SynchronizedProperty<Boolean>) CHARGE_JUMP_TRIGGERED.get(action)).getOrDefaultIfNull(Boolean.FALSE);
        } catch (ReflectiveOperationException | ClassCastException e) {
            return true;
        }
    }

    @Nullable
    private static Field chargeJumpTriggeredField() {
        try {
            Field field = ChargeJump.class.getDeclaredField("propertyJumpTriggered");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException e) {
            Constants.LOG.warn("Could not find ChargeJump.propertyJumpTriggered, ParCool charge jumps will always cost stamina. Someone tell Evan!", e);
            return null;
        }
    }
}
//?}
