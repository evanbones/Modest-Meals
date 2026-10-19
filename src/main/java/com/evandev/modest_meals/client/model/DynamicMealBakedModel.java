package com.evandev.modest_meals.client.model;

import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.food.ingredient.MealIngredientManager;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.CompositeModel;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Dynamically composites layer textures onto a meal item based on its {@link MealContents} component.
 */
public class DynamicMealBakedModel extends BakedModelWrapper<BakedModel> {

    private final ResourceLocation mealTypeId;
    private final ItemOverrides overrides;

    public DynamicMealBakedModel(BakedModel originalModel, ResourceLocation mealTypeId) {
        super(originalModel);
        this.mealTypeId = mealTypeId;
        this.overrides = new DynamicMealItemOverrides(originalModel);
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    private class DynamicMealItemOverrides extends ItemOverrides {
        private final BakedModel baseModel;
        private final Map<String, BakedModel> cache = new ConcurrentHashMap<>();

        DynamicMealItemOverrides(BakedModel baseModel) {
            this.baseModel = baseModel;
        }

        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
            MealContents contents = stack.get(ModDataComponents.MEAL_CONTENTS.get());
            if (contents == null) {
                return baseModel;
            }

            Optional<MealType> maybeType = MealTypeManager.get(mealTypeId);
            if (maybeType.isEmpty()) {
                return baseModel;
            }
            MealType type = maybeType.get();
            Function<ResourceLocation, TextureAtlasSprite> spriteGetter =
                    Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);

            if (contents.dubious()) {
                if (type.dubiousSprite().isEmpty()) {
                    return baseModel;
                }
                String key = "dubious:" + mealTypeId;
                return cache.computeIfAbsent(key, k -> {
                    ResourceLocation dubiousSpriteLoc = type.dubiousSprite().get();
                    TextureAtlasSprite sprite = spriteGetter.apply(dubiousSpriteLoc);
                    return bakeLayered(baseModel, List.of(sprite));
                });
            }

            if (contents.ingredients().isEmpty()) {
                return baseModel;
            }

            for (MealType.SpecialSprite special : type.specialSprites()) {
                if (special.matches(contents.ingredients())) {
                    String key = "special:" + special.sprite();
                    return cache.computeIfAbsent(key, k -> {
                        TextureAtlasSprite sprite = spriteGetter.apply(special.sprite());
                        return bakeLayered(baseModel, List.of(sprite));
                    });
                }
            }

            if (type.baseSprites().isEmpty()) {
                return baseModel;
            }

            MealType.BaseSprites baseSprites = type.baseSprites().get();
            String cacheKey = String.join(",", contents.ingredients().stream().map(ResourceLocation::toString).toList());
            return cache.computeIfAbsent(cacheKey, k -> {
                List<TextureAtlasSprite> sprites = new ArrayList<>();
                baseSprites.bottom().ifPresent(bottom -> sprites.add(spriteGetter.apply(bottom)));

                int slots = baseSprites.getEffectiveLayerSlots(type.maxIngredients());
                for (int i = 0; i < slots; i++) {
                    ResourceLocation fillingLoc = getIngredientSprite(contents.ingredients(), i, slots, type);
                    if (fillingLoc != null) {
                        sprites.add(spriteGetter.apply(fillingLoc));
                    }
                }

                baseSprites.top().ifPresent(top -> sprites.add(spriteGetter.apply(top)));

                return bakeLayered(baseModel, sprites);
            });
        }

        private @Nullable ResourceLocation getIngredientSprite(List<ResourceLocation> ingredients, int index, int totalSlots, MealType type) {
            if (index < ingredients.size()) {
                ResourceLocation itemId = ingredients.get(index);
                Item item = BuiltInRegistries.ITEM.get(itemId);
                Optional<ResourceLocation> sprite = MealIngredientManager.getIngredientSprite(mealTypeId, item, index, totalSlots);
                if (sprite.isPresent()) {
                    return sprite.get();
                }
                if (type.requiresSupportedIngredients()) {
                    return type.dubiousSprite().orElse(null);
                }
            }
            return null;
        }

        private BakedModel bakeLayered(BakedModel original, List<TextureAtlasSprite> sprites) {
            TextureAtlasSprite particle = sprites.isEmpty() || sprites.getFirst() == null ? original.getParticleIcon() : sprites.getFirst();
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
                if (sprite == null) {
                    continue;
                }
                var unbaked = UnbakedGeometryHelper.createUnbakedItemElements(i, sprite);
                var quads = UnbakedGeometryHelper.bakeElements(unbaked, $ -> sprite, BlockModelRotation.X0_Y0);
                builder.addQuads(normalRenderTypes, quads);
            }

            return builder.build();
        }
    }
}
