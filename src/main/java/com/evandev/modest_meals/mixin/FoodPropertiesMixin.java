package com.evandev.modest_meals.mixin;

//? if >=26.1 {
/*import com.evandev.modest_meals.config.ModConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodProperties.class)
public class FoodPropertiesMixin {
    @WrapOperation(method = "onConsume", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
    private void mm$playerEatFood(FoodData foodData, FoodProperties foodProperties, Operation<Void> original) {
        if (!ModConfig.get().disableHunger) {
            original.call(foodData, foodProperties);
        }
    }
}
*///?}
