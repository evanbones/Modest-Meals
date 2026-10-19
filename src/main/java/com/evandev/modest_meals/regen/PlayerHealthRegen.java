package com.evandev.modest_meals.regen;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.effect.ModMobEffects;
import com.evandev.modest_meals.network.ClientboundHealthRegenSyncPayload;
import com.evandev.modest_meals.network.ModNetworking;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.List;

@ApiStatus.Internal
public class PlayerHealthRegen {

    private static final String NBT_CONSUMED_FOODS = "consumedFoods";
    private static final String NBT_CONSUMED_NUTRITION = "consumedNutrition";

    private final Player player;
    private HashSet<ConsumedFood> consumedFoods = new HashSet<>();
    private int consumedNutrition = 0;

    public PlayerHealthRegen(Player player) {
        this.player = player;
    }

    public void readFromNbt(CompoundTag tag) {
        this.consumedNutrition = Math.max(tag.getInt(NBT_CONSUMED_NUTRITION), 0);
        this.consumedFoods = new HashSet<>();

        Tag foods = tag.get(NBT_CONSUMED_FOODS);
        if (foods == null) {
            return;
        }
        ConsumedFood.CODEC.listOf().parse(NbtOps.INSTANCE, foods)
                .resultOrPartial(error -> Constants.LOG.error("Failed to parse digesting food: {}", error))
                .ifPresent(this.consumedFoods::addAll);
    }

    public void writeToNbt(CompoundTag tag) {
        tag.putInt(NBT_CONSUMED_NUTRITION, this.consumedNutrition);
        ConsumedFood.CODEC.listOf().encodeStart(NbtOps.INSTANCE, List.copyOf(this.consumedFoods))
                .resultOrPartial(error -> Constants.LOG.error("Failed to serialize digesting food: {}", error))
                .ifPresent(encoded -> tag.put(NBT_CONSUMED_FOODS, encoded));
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

        HashSet<String> digestingFoods = new HashSet<>();
        boolean needsSync = false;

        var iterator = consumedFoods.iterator();
        while (iterator.hasNext()) {
            ConsumedFood consumedFood = iterator.next();
            String consumedFoodId = consumedFood.getFoodId();
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

    public void addHealth(float points, int digestTicks, String foodId) {
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
        public static final Codec<ConsumedFood> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("food_id").forGetter(food -> food.foodId),
                Codec.INT.fieldOf("nutrition").forGetter(food -> food.foodNutrition),
                Codec.INT.fieldOf("ticks_to_heal").forGetter(food -> food.ticksToHeal),
                Codec.INT.optionalFieldOf("digested", 0).forGetter(food -> food.digestedNutrition),
                Codec.INT.optionalFieldOf("counter", 0).forGetter(food -> food.ticksCounter)
        ).apply(instance, ConsumedFood::new));

        private final String foodId;
        private final int foodNutrition;
        private final int ticksToHeal;
        private int digestedNutrition;
        private int ticksCounter;

        public ConsumedFood(int foodNutrition, int digestTicks, String foodId) {
            this.foodId = foodId;
            this.foodNutrition = foodNutrition;
            float speed = Math.max(0.01F, ModConfig.get().gradualHealthRegenerationSpeed);
            this.ticksToHeal = Math.max(1, (int) (digestTicks / (float) foodNutrition / speed));
            this.digestedNutrition = 0;
            this.ticksCounter = 0;
        }

        private ConsumedFood(String foodId, int foodNutrition, int ticksToHeal,
                             int digestedNutrition, int ticksCounter) {
            this.foodId = foodId;
            this.foodNutrition = foodNutrition;
            this.ticksToHeal = ticksToHeal;
            this.digestedNutrition = digestedNutrition;
            this.ticksCounter = ticksCounter;
        }

        public String getFoodId() {
            return foodId;
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
