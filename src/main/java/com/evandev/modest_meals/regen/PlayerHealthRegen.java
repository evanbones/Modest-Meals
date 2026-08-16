package com.evandev.modest_meals.regen;

import com.evandev.modest_meals.compat.farmers_delight.FarmersDelightCompat;
import com.evandev.modest_meals.compat.farmers_delight.NourishmentEffectHandler;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.network.ClientboundHealthRegenSyncPayload;
import com.evandev.modest_meals.network.ModNetworking;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;

public class PlayerHealthRegen {

    public static class ConsumedFood {
        private final int foodComponentId;
        private final int foodNutrition;
        private int digestedNutrition = 0;
        private int ticksCounter = 0;
        private final int ticksToHeal;

        public ConsumedFood(int foodNutrition, float foodSaturation, int foodComponentId) {
            this.foodComponentId = foodComponentId;
            this.foodNutrition = foodNutrition;
            if (ModConfig.get().saturationBasedRegeneration) {
                float ratio = Math.min(5.0F, foodNutrition / Math.max(0.1F, foodSaturation));
                this.ticksToHeal = Math.max(
                        1, (int) (ratio * 20 / Math.max(0.01F, ModConfig.get().gradualHealthRegenerationSpeed))
                );
            } else {
                this.ticksToHeal = Math.max(
                        1, (int) (20 / Math.max(0.01F, ModConfig.get().gradualHealthRegenerationSpeed))
                );
            }
        }

        public int getFoodComponentId() {
            return foodComponentId;
        }

        public boolean isFullyDigested() {
            return digestedNutrition >= foodNutrition;
        }

        public boolean tick(float regenSpeedMultiplier) {
            int effectiveTicksToHeal = Math.max(1, (int) (this.ticksToHeal / Math.max(0.01F, regenSpeedMultiplier)));
            if (ticksCounter < effectiveTicksToHeal) {
                ticksCounter++;
                return false;
            }
            digestedNutrition++;
            ticksCounter = 0;
            return true;
        }
    }

    private static final TypeToken<HashSet<ConsumedFood>> CONSUMED_FOOD_SET_TYPE = new TypeToken<>() {};
    private static final Gson GSON = new Gson();

    private final Player player;
    private HashSet<ConsumedFood> consumedFoods = new HashSet<>();
    private int consumedNutrition = 0;

    public PlayerHealthRegen(Player player) {
        this.player = player;
    }

    public void readFromNbt(CompoundTag tag) {
        this.consumedNutrition = Math.max(tag.getInt("consumedNutrition"), 0);
        String consumedFoodsStr = tag.getString("consumedFoods");
        if (!consumedFoodsStr.isEmpty()) {
            try {
                this.consumedFoods = GSON.fromJson(consumedFoodsStr, CONSUMED_FOOD_SET_TYPE.getType());
                if (this.consumedFoods == null) {
                    this.consumedFoods = new HashSet<>();
                }
            } catch (Exception ignored) {
                this.consumedFoods = new HashSet<>();
            }
        }
    }

    public void writeToNbt(CompoundTag tag) {
        tag.putInt("consumedNutrition", this.consumedNutrition);
        tag.putString("consumedFoods", GSON.toJson(this.consumedFoods));
    }

    public void sync() {
        if (player instanceof ServerPlayer serverPlayer) {
            ModNetworking.sendToPlayer(serverPlayer, new ClientboundHealthRegenSyncPayload(this.consumedNutrition));
        }
    }

    public void setClientConsumedNutrition(int consumedNutrition) {
        this.consumedNutrition = consumedNutrition;
    }

    public void serverTick() {
        if (!ModConfig.get().disableHunger || !ModConfig.get().gradualHealthRegeneration) {
            return;
        }
        if (consumedFoods.isEmpty()) {
            if (consumedNutrition != 0) {
                consumedNutrition = 0;
                sync();
            }
            return;
        }
        if (player.getHealth() >= player.getMaxHealth()) {
            // Player is at full health
            switch (ModConfig.get().regenerationAtFullHealth) {
                case STOPPED -> {
                    consumedFoods.clear();
                    consumedNutrition = 0;
                    sync();
                    return;
                }
                case STORED -> {
                    return;
                }
                case CONTINUED -> {
                    // Continues digesting below
                }
            }
        }

        HashSet<Integer> digestingFoods = new HashSet<>();
        boolean needsSync = false;
        float regenSpeedMultiplier = 1.0F;
        if (FarmersDelightCompat.isLoaded() && NourishmentEffectHandler.playerHasEffect(player)) {
            regenSpeedMultiplier = ModConfig.get().nourishmentRegenSpeedMultiplier;
        }

        var iterator = consumedFoods.iterator();
        while (iterator.hasNext()) {
            ConsumedFood consumedFood = iterator.next();
            int consumedFoodId = consumedFood.getFoodComponentId();
            if (digestingFoods.contains(consumedFoodId)) {
                // Parallel healing only with unique food types
                continue;
            }
            digestingFoods.add(consumedFoodId);
            if (!consumedFood.tick(regenSpeedMultiplier)) {
                continue;
            }
            if (consumedNutrition > 0) {
                player.heal(1.0F);
                consumedNutrition--;
                needsSync = true;
            }
            if (consumedFood.isFullyDigested()) {
                iterator.remove();
            }
        }

        if (needsSync) {
            sync();
        }
    }

    public boolean canEat() {
        if (!ModConfig.get().disableHunger) {
            return player.getFoodData().needsFood();
        }
        if (!ModConfig.get().gradualHealthRegeneration) {
            return player.getHealth() < player.getMaxHealth();
        }
        return player.getHealth() + consumedNutrition < player.getMaxHealth();
    }

    public void eat(int foodHealth, float foodSaturation, int foodComponentId) {
        if (ModConfig.get().gradualHealthRegeneration) {
            consumedNutrition += foodHealth;
            consumedFoods.add(new ConsumedFood(foodHealth, foodSaturation, foodComponentId));
            sync();
        } else {
            player.heal(foodHealth);
        }
    }

    public boolean eat(ItemStack itemStack, FoodProperties foodComponent) {
        if (!(player instanceof ServerPlayer) || !ModConfig.get().disableHunger) {
            return false;
        }
        int foodHealth = ModConfig.getFoodHealth(itemStack, foodComponent);
        eat(foodHealth, foodComponent.saturation(), foodComponent.hashCode());
        return true;
    }

    public int getConsumedNutrition() {
        if (!ModConfig.get().gradualHealthRegeneration) {
            return 0;
        }
        return consumedNutrition;
    }

    public void reset() {
        consumedFoods.clear();
        consumedNutrition = 0;
        sync();
    }
}
