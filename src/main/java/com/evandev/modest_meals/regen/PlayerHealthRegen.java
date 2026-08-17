package com.evandev.modest_meals.regen;

import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.effect.ModMobEffects;
import com.evandev.modest_meals.network.ClientboundHealthRegenSyncPayload;
import com.evandev.modest_meals.network.ModNetworking;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;

public class PlayerHealthRegen {

    private static final TypeToken<HashSet<ConsumedFood>> CONSUMED_FOOD_SET_TYPE = new TypeToken<>() {
    };
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
        if (player.hasEffect(ModMobEffects.HEALTH_NO_REGEN)) {
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

        var iterator = consumedFoods.iterator();
        while (iterator.hasNext()) {
            ConsumedFood consumedFood = iterator.next();
            int consumedFoodId = consumedFood.getFoodComponentId();
            if (digestingFoods.contains(consumedFoodId)) {
                // Parallel healing only with unique food types
                continue;
            }
            digestingFoods.add(consumedFoodId);
            if (!consumedFood.tick()) {
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

    public boolean hasHealthRoom() {
        if (!ModConfig.get().disableHunger) {
            return player.getFoodData().needsFood();
        }
        return player.getHealth() < player.getMaxHealth();
    }

    public void addHealth(float points, int digestTicks, int foodId) {
        if (player.level().isClientSide()) {
            return;
        }
        int wholePoints = Mth.ceil(points);
        if (wholePoints <= 0) {
            return;
        }
        if (ModConfig.get().disableHunger && ModConfig.get().gradualHealthRegeneration && digestTicks > 0) {
            consumedNutrition += wholePoints;
            consumedFoods.add(new ConsumedFood(wholePoints, digestTicks, foodId));
            sync();
        } else {
            player.heal(points);
        }
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

    public static class ConsumedFood {
        private final int foodComponentId;
        private final int foodNutrition;
        private final int ticksToHeal;
        private int digestedNutrition = 0;
        private int ticksCounter = 0;

        public ConsumedFood(int foodNutrition, int digestTicks, int foodComponentId) {
            this.foodComponentId = foodComponentId;
            this.foodNutrition = foodNutrition;
            float speed = Math.max(0.01F, ModConfig.get().gradualHealthRegenerationSpeed);
            this.ticksToHeal = Math.max(1, (int) (digestTicks / (float) foodNutrition / speed));
        }

        public int getFoodComponentId() {
            return foodComponentId;
        }

        public boolean isFullyDigested() {
            return digestedNutrition >= foodNutrition;
        }

        public boolean tick() {
            if (ticksCounter < this.ticksToHeal) {
                ticksCounter++;
                return false;
            }
            digestedNutrition++;
            ticksCounter = 0;
            return true;
        }
    }
}
