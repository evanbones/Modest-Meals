package com.evandev.modest_meals.food;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EdibleBlockContext {
    private static final Map<UUID, Context> SERVER = new ConcurrentHashMap<>();
    private static final Map<UUID, Context> CLIENT = new ConcurrentHashMap<>();

    @Nullable
    public static Context set(Player player, BlockState state) {
        Context context = new Context(state, EdibleBlockFoods.resolve(state).orElse(null));
        return map(player).put(player.getUUID(), context);
    }

    public static Optional<Context> get(Player player) {
        return Optional.ofNullable(map(player).get(player.getUUID()));
    }

    public static void restore(Player player, @Nullable Context previous) {
        if (previous == null) {
            map(player).remove(player.getUUID());
        } else {
            map(player).put(player.getUUID(), previous);
        }
    }

    private static Map<UUID, Context> map(Player player) {
        return player.level().isClientSide() ? CLIENT : SERVER;
    }

    public record Context(BlockState state, @Nullable EdibleBlockFoods.EdibleBlock food) {
    }
}
