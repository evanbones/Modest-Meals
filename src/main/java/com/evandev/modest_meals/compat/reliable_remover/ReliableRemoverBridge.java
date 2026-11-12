package com.evandev.modest_meals.compat.reliable_remover;

import com.evandev.reliable_remover.api.ReliableRemoverAPI;
import net.minecraft.world.item.Item;

final class ReliableRemoverBridge {

    static boolean isRemoved(Item item) {
        return ReliableRemoverAPI.isLootBlocked(item);
    }

    static boolean isHidden(Item item) {
        return ReliableRemoverAPI.isItemHidden(item);
    }
}
