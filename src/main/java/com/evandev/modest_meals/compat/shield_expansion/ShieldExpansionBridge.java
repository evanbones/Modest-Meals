package com.evandev.modest_meals.compat.shield_expansion;

//? if shield_expansion {
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.infernalstudios.shieldexp.access.LivingEntityAccess;
import org.infernalstudios.shieldexp.config.ShieldExpansionConfig;
import org.infernalstudios.shieldexp.events.ShieldEvents;

final class ShieldExpansionBridge {

    static boolean isShield(ItemStack stack) {
        return !ShieldExpansionConfig.ITEM_ONLY_MODE && ShieldExpansionConfig.isShield(stack.getItem());
    }

    static boolean willParry(Player player, DamageSource source) {
        return inParryWindow(player) && player.isDamageSourceBlocked(source);
    }

    static boolean willParry(Player player, Entity projectile) {
        return inParryWindow(player) && ShieldEvents.canBlockProjectile(player, projectile);
    }

    private static boolean inParryWindow(Player player) {
        return !ShieldExpansionConfig.ITEM_ONLY_MODE
                && ShieldEvents.validateBlocking(player)
                && LivingEntityAccess.get(player).shieldexp$getParryWindow() > 0;
    }
}
//?}
