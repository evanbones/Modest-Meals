package com.evandev.modest_meals.client.model;

import com.evandev.modest_meals.Constants;
import com.evandev.modest_meals.block.PizzaBlock;
import com.evandev.modest_meals.block.PizzaBlockEntity;
import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.food.ingredient.MealIngredientManager;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import com.evandev.modest_meals.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lays a placed pizza's toppings over the slices that are still there.
 */
public class PizzaRenderer implements BlockEntityRenderer<PizzaBlockEntity> {
    private static final float FLOOR = 1.0F / 16.0F + 0.001F;
    private static final ResourceLocation VISUALS =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "placed_pizza");
    private static final ResourceLocation DUBIOUS =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/meal/placed_pizza/dubious");

    public PizzaRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PizzaBlockEntity pizza, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int light, int overlay) {
        List<TextureAtlasSprite> toppings = toppingsOf(pizza.contents());
        if (toppings.isEmpty()) {
            return;
        }
        int bites = pizza.getBlockState().getValue(PizzaBlock.BITES);
        VertexConsumer consumer = buffers.getBuffer(Sheets.cutoutBlockSheet());
        PoseStack.Pose pose = poseStack.last();
        for (TextureAtlasSprite sprite : toppings) {
            for (int quadrant = bites; quadrant < PizzaBlock.SLICES; quadrant++) {
                int[] q = PizzaBlock.QUADRANTS[quadrant];
                quad(consumer, pose, sprite, q[0], q[1], q[2], q[3], light, overlay);
            }
        }
    }

    private static List<TextureAtlasSprite> toppingsOf(MealContents contents) {
        var atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        if (contents.dubious()) {
            return List.of(atlas.apply(DUBIOUS));
        }
        if (contents.ingredients().isEmpty()) {
            return List.of();
        }
        ResourceLocation pizzaId = BuiltInRegistries.ITEM.getKey(ModItems.PIZZA.get());
        Optional<MealType> type = MealTypeManager.get(pizzaId);
        if (type.isEmpty() || type.get().baseSprites().isEmpty()) {
            return List.of();
        }
        int slots = type.get().baseSprites().get().getEffectiveLayerSlots(type.get().maxIngredients());
        List<TextureAtlasSprite> sprites = new ArrayList<>();
        for (int i = 0; i < Math.min(slots, contents.ingredients().size()); i++) {
            int slot = i;
            BuiltInRegistries.ITEM.getOptional(contents.ingredients().get(i))
                    .flatMap(item -> MealIngredientManager.getIngredientSprite(VISUALS, item, slot, slots))
                    .ifPresent(sprite -> sprites.add(atlas.apply(sprite)));
        }
        return sprites;
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite,
                             int x0, int z0, int x1, int z1, int light, int overlay) {
        vertex(consumer, pose, sprite, x0, z0, light, overlay);
        vertex(consumer, pose, sprite, x0, z1, light, overlay);
        vertex(consumer, pose, sprite, x1, z1, light, overlay);
        vertex(consumer, pose, sprite, x1, z0, light, overlay);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, TextureAtlasSprite sprite,
                               int x, int z, int light, int overlay) {
        consumer.addVertex(pose, x / 16.0F, FLOOR, z / 16.0F)
                .setColor(255, 255, 255, 255)
                .setUv(sprite.getU(x / 16.0F), sprite.getV(z / 16.0F))
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
