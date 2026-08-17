package com.evandev.modest_meals.mixin;

import com.evandev.modest_meals.client.tooltip.IconRowTooltip;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientTooltipComponent.class)
public interface TooltipComponentMixin {
    @WrapMethod(
            method = "create(Lnet/minecraft/util/FormattedCharSequence;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;"
    )
    private static ClientTooltipComponent mm$createTooltipComponent(FormattedCharSequence text, Operation<ClientTooltipComponent> original) {
        if (text instanceof IconRowTooltip.Marker marker) {
            return marker.toTooltip();
        }
        return original.call(text);
    }
}
