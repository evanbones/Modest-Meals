package com.evandev.modest_meals.stamina;

import com.evandev.modest_meals.config.ModConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;

public class ShieldBlocking {

    public static boolean isBlocking(Player player) {
        return ModConfig.get().staminaShieldBlocking
                && player.isUsingItem()
                && player.getUseItem().getUseAnimation() == UseAnim.BLOCK;
    }
}
