package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @WrapMethod(method = "getMaxStackSize")
    private int mm$customStackSize(Operation<Integer> original) {
        Integer stackSize = ModConfig.getItemStackSize((ItemStack) (Object) this);
        return stackSize != null ? stackSize : original.call();
    }
}
