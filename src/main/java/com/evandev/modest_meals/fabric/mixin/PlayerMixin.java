package com.evandev.modest_meals.fabric.mixin;

//? if fabric {
/*import com.evandev.modest_meals.attribute.ModAttributes;
import com.evandev.modest_meals.event.CommonEvents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @ModifyReturnValue(method = "createAttributes", at = @At("RETURN"))
    private static AttributeSupplier.Builder mm$addStaminaCapacity(AttributeSupplier.Builder builder) {
        return builder.add(ModAttributes.STAMINA_CAPACITY);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void mm$onTick(CallbackInfo ci) {
        CommonEvents.onPlayerTick((Player) (Object) this);
    }
}
*///?}
