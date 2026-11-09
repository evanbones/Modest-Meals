package com.evandev.modest_meals.client;

import com.evandev.modest_meals.block.MealBlockEntity;
import com.evandev.modest_meals.block.SlicedMealBlock;
import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.food.ingredient.MealIngredientManager;
import com.evandev.modest_meals.item.MealItem;
import com.evandev.modest_meals.mixin.SpriteContentsAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
//? if >=26.1
/*import net.minecraft.client.color.block.BlockTintSource;*/
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MealBlockColors {
    private static final int PLAIN = 0xFFFFFF;
    private static final int DUBIOUS = 0x7E8F3A;
    private static final Map<ResourceLocation, Integer> SPRITE_COLORS = new ConcurrentHashMap<>();

    //? if >=26.1 {
    /*public static final BlockTintSource TINT = new BlockTintSource() {
        @Override
        public int color(BlockState state) {
            return 0xFF000000 | PLAIN;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return 0xFF000000 | MealBlockColors.color(state, level, pos, 0);
        }
    };
    *///?}

    public static int color(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (level == null || pos == null || !(level.getBlockEntity(pos) instanceof MealBlockEntity meal)) {
            return PLAIN;
        }
        MealContents contents = meal.contents();
        if (contents.dubious()) {
            return DUBIOUS;
        }
        ResourceLocation mealId = BuiltInRegistries.ITEM.getKey(((SlicedMealBlock) state.getBlock()).asItem());
        return MealItem.mainIngredient(contents.ingredients())
                .flatMap(BuiltInRegistries.ITEM::getOptional)
                .flatMap(item -> MealIngredientManager.getIngredientSprite(mealId, item, 0, 1))
                .map(sprite -> SPRITE_COLORS.computeIfAbsent(sprite, MealBlockColors::averageColor))
                .orElse(PLAIN);
    }

    public static void clearCache() {
        SPRITE_COLORS.clear();
    }

    private static int averageColor(ResourceLocation spriteId) {
        TextureAtlasSprite sprite = RenderCompat.itemSprite(spriteId);
        NativeImage image = ((SpriteContentsAccessor) sprite.contents()).mm$getOriginalImage();
        long red = 0;
        long green = 0;
        long blue = 0;
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int abgr = RenderCompat.pixelAbgr(image, x, y);
                if ((abgr >>> 24) < 128) {
                    continue;
                }
                red += abgr & 0xFF;
                green += (abgr >> 8) & 0xFF;
                blue += (abgr >> 16) & 0xFF;
                count++;
            }
        }
        if (count == 0) {
            return PLAIN;
        }
        return (int) (red / count) << 16 | (int) (green / count) << 8 | (int) (blue / count);
    }
}
