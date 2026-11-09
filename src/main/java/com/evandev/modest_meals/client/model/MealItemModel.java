package com.evandev.modest_meals.client.model;

//? if >=26.1 {
/*import com.evandev.modest_meals.client.RenderCompat;
import com.mojang.math.Quadrant;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.geometry.BakedQuad;
//? if >=26.3
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class MealItemModel implements ItemModel {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("modest_meals", "meal");
    private static final float LAYER_DEPTH_OFFSET = 0.05F;
    private static final float MIN_Z = 7.5F;
    private static final float MAX_Z = 8.5F;
    private static final CuboidFace.UVs SOUTH_FACE_UVS = new CuboidFace.UVs(0.0F, 0.0F, 16.0F, 16.0F);
    private static final CuboidFace.UVs NORTH_FACE_UVS = new CuboidFace.UVs(16.0F, 0.0F, 0.0F, 16.0F);
    private static final ModelBaker.Interner IDENTITY_INTERNER = new ModelBaker.Interner() {
        @Override
        public Vector3fc vector(Vector3fc vector) {
            return vector;
        }

        @Override
        public BakedQuad.MaterialInfo materialInfo(BakedQuad.MaterialInfo material) {
            return material;
        }
    };

    private final ItemModel fallback;
    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;
    private final Map<String, List<BakedQuad>> cache = new ConcurrentHashMap<>();

    private MealItemModel(ItemModel fallback, ModelRenderProperties properties, Matrix4fc transformation) {
        this.fallback = fallback;
        this.properties = properties;
        this.transformation = transformation;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext,
                       @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        Optional<MealModelSpec.Layers> layers = MealModelSpec.forItem(item.getItem()).resolve(item);
        if (layers.isEmpty()) {
            fallback.update(output, item, resolver, displayContext, level, owner, seed);
            return;
        }
        List<BakedQuad> quads = cache.computeIfAbsent(item.getItem() + "|" + layers.get().key(),
                key -> bakeLayers(layers.get().sprites()));
        output.appendModelIdentityElement(this);
        output.appendModelIdentityElement(layers.get().key());
        ItemStackRenderState.LayerRenderState layer = output.newLayer();
        layer.setLocalTransform(this.transformation);
        this.properties.applyToLayer(layer, displayContext);
        //? if >=26.3 {
        layer.setQuads(ItemQuads.split(quads));
        //?} else
        layer.prepareQuadList().addAll(quads);
    }

    private static List<BakedQuad> bakeLayers(List<ResourceLocation> spriteIds) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (int i = 0; i < spriteIds.size(); i++) {
            TextureAtlasSprite sprite = RenderCompat.itemSprite(spriteIds.get(i));
            //? if >=26.3 {
            BakedQuad.MaterialInfo material = new BakedQuad.MaterialInfo(sprite, ChunkSectionLayer.TRANSLUCENT,
                    Sheets.translucentItemSheet(), Sheets.translucentItemGlintSheet(), Sheets.translucentItemGlintSpecialSheet(),
                    -1, null, 0);
            //?} else {
            BakedQuad.MaterialInfo material = new BakedQuad.MaterialInfo(sprite, ChunkSectionLayer.TRANSLUCENT,
                    Sheets.translucentItemSheet(), -1, true, 0);
            //?}
            float offset = i * LAYER_DEPTH_OFFSET;
            Vector3f from = new Vector3f(0.0F, 0.0F, MIN_Z - offset);
            Vector3f to = new Vector3f(16.0F, 16.0F, MAX_Z + offset);
            builder.addUnculledFace(FaceBakery.bakeQuad(IDENTITY_INTERNER, from, to, SOUTH_FACE_UVS, Quadrant.R0, material,
                    Direction.SOUTH, BlockModelRotation.IDENTITY, null));
            builder.addUnculledFace(FaceBakery.bakeQuad(IDENTITY_INTERNER, from, to, NORTH_FACE_UVS, Quadrant.R0, material,
                    Direction.NORTH, BlockModelRotation.IDENTITY, null));
            ItemModelGenerator.bakeSideFaces(builder, IDENTITY_INTERNER, BlockModelRotation.IDENTITY, material);
        }
        return new ArrayList<>(builder.build().getAll());
    }

    public record Unbaked(ResourceLocation model) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("model").forGetter(Unbaked::model)
        ).apply(instance, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ItemModel fallback = new CuboidItemModelWrapper.Unbaked(this.model, Optional.empty(), List.of()).bake(context, transformation);
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel resolved = baker.getModel(this.model);
            TextureSlots textureSlots = resolved.getTopTextureSlots();
            ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolved, textureSlots);
            return new MealItemModel(fallback, properties, transformation);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
*///?}
