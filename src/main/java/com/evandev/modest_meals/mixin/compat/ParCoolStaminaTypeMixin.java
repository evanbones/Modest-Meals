package com.evandev.modest_meals.mixin.compat;

import com.evandev.modest_meals.compat.parcool.ParCoolCompat;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.alrex.parcool.config.ParCoolConfig$Server", remap = false)
public abstract class ParCoolStaminaTypeMixin {

    @Unique
    private static final ResourceLocation mm$PARCOOL_STAMINA = ResourceLocation.fromNamespaceAndPath("parcool", "parcool");

    @Inject(method = "getStaminaTypeID", at = @At("RETURN"), cancellable = true)
    private void mm$useModestMealsStamina(CallbackInfoReturnable<ResourceLocation> cir) {
        if (mm$PARCOOL_STAMINA.equals(cir.getReturnValue()) && ParCoolCompat.isActive()) {
            cir.setReturnValue(ResourceLocation.parse(ParCoolCompat.STAMINA_TYPE));
        }
    }
}
