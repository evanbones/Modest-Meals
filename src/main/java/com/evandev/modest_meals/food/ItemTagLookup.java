package com.evandev.modest_meals.food;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.Optional;

@FunctionalInterface
public interface ItemTagLookup {
    ItemTagLookup UNKNOWN = tag -> Optional.empty();

    Optional<Collection<Holder<Item>>> members(TagKey<Item> tag);

    //? if neoforge {
    static ItemTagLookup of(net.neoforged.neoforge.common.conditions.ICondition.IContext context) {
        if (context == net.neoforged.neoforge.common.conditions.ICondition.IContext.EMPTY
                || context == net.neoforged.neoforge.common.conditions.ICondition.IContext.TAGS_INVALID) {
            return UNKNOWN;
        }
        return tag -> Optional.of(context.getTag(tag));
    }
    //?}
}
