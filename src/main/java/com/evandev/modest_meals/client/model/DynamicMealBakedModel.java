package com.evandev.modest_meals.client.model;

//? if <26.1 {
import com.evandev.modest_meals.client.RenderCompat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

//? if neoforge {
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
//?} else {
/*import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
*///?}

//? if neoforge {
public class DynamicMealBakedModel extends BakedModelWrapper<BakedModel> {
//?} else
//public class DynamicMealBakedModel extends ForwardingBakedModel {
    private static final float LAYER_DEPTH_OFFSET = 0.05F;
    //? if fabric {
    /*private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final Map<Direction, List<BakedQuad>> EMPTY_CULLED_FACES = Arrays.stream(Direction.values())
            .collect(Collectors.toMap(direction -> direction, direction -> List.of()));
    *///?}

    private final ItemOverrides overrides;

    private DynamicMealBakedModel(BakedModel originalModel, MealModelSpec spec) {
        //? if neoforge {
        super(originalModel);
        //?} else
        //this.wrapped = originalModel;
        this.overrides = new DynamicMealItemOverrides(originalModel, spec);
    }

    public static BakedModel wrap(Supplier<Item> meal, BakedModel original) {
        return new DynamicMealBakedModel(original, MealModelSpec.forItem(meal.get()));
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    private static class DynamicMealItemOverrides extends ItemOverrides {
        private final BakedModel baseModel;
        private final MealModelSpec spec;
        private final Map<String, BakedModel> cache = new ConcurrentHashMap<>();

        DynamicMealItemOverrides(BakedModel baseModel, MealModelSpec spec) {
            this.baseModel = baseModel;
            this.spec = spec;
        }

        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            return spec.resolve(stack)
                    .map(layers -> cache.computeIfAbsent(layers.key(),
                            key -> bakeLayered(baseModel, layers.sprites().stream().map(RenderCompat::itemSprite).toList())))
                    .orElse(baseModel);
        }

        private static BakedModel bakeLayered(BakedModel original, List<TextureAtlasSprite> sprites) {
            TextureAtlasSprite particle = sprites.isEmpty() ? original.getParticleIcon() : sprites.getFirst();
            //? if neoforge {
            var normalRenderTypes = new RenderTypeGroup(RenderType.translucent(), NeoForgeRenderTypes.ITEM_UNSORTED_TRANSLUCENT.get());
            CompositeModel.Baked.Builder builder = CompositeModel.Baked.builder(
                    original.useAmbientOcclusion(),
                    false,
                    false,
                    particle,
                    ItemOverrides.EMPTY,
                    original.getTransforms()
            );
            for (int i = 0; i < sprites.size(); i++) {
                TextureAtlasSprite sprite = sprites.get(i);
                List<BlockElement> unbaked = UnbakedGeometryHelper.createUnbakedItemElements(i, sprite);
                float offset = i * LAYER_DEPTH_OFFSET;
                for (BlockElement element : unbaked) {
                    element.from.z -= offset;
                    element.to.z += offset;
                }
                builder.addQuads(normalRenderTypes, UnbakedGeometryHelper.bakeElements(unbaked, $ -> sprite, BlockModelRotation.X0_Y0));
            }
            return builder.build();
            //?} else {
            /*List<BakedQuad> quads = new ArrayList<>();
            for (int i = 0; i < sprites.size(); i++) {
                TextureAtlasSprite sprite = sprites.get(i);
                float offset = i * LAYER_DEPTH_OFFSET;
                for (BlockElement element : ITEM_MODEL_GENERATOR.processFrames(i, "layer" + i, sprite.contents())) {
                    element.from.z -= offset;
                    element.to.z += offset;
                    element.faces.forEach((direction, face) -> quads.add(FACE_BAKERY.bakeQuad(
                            element.from, element.to, face, sprite, direction, BlockModelRotation.X0_Y0, element.rotation, element.shade)));
                }
            }
            return new SimpleBakedModel(quads, EMPTY_CULLED_FACES, original.useAmbientOcclusion(), false, false,
                    particle, original.getTransforms(), ItemOverrides.EMPTY);
            *///?}
        }
    }
}
//?}
