package com.evandev.modest_meals.compat.reliable_remover;

import com.evandev.reliable_remover.api.ReliableRemoverAPI;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class ReliableRemoverBridge {

    static boolean isRemoved(Item item) {
        return ReliableRemoverAPI.isLootBlocked(new ItemStack(item));
    }
}
