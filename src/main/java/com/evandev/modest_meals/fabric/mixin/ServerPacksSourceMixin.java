package com.evandev.modest_meals.fabric.mixin;

//? if fabric {
/*import com.evandev.modest_meals.food.CustomFoodDatapack;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Arrays;

@Mixin(ServerPacksSource.class)
public abstract class ServerPacksSourceMixin {
    @ModifyArg(method = "createPackRepository(Ljava/nio/file/Path;Lnet/minecraft/world/level/validation/DirectoryValidator;)Lnet/minecraft/server/packs/repository/PackRepository;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;<init>([Lnet/minecraft/server/packs/repository/RepositorySource;)V"))
    private static RepositorySource[] mm$addCustomFoodPack(RepositorySource[] sources) {
        RepositorySource[] extended = Arrays.copyOf(sources, sources.length + 1);
        extended[sources.length] = consumer -> CustomFoodDatapack.createPack().ifPresent(consumer);
        return extended;
    }
}
*///?}
