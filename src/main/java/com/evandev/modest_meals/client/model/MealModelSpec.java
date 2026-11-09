package com.evandev.modest_meals.client.model;

import com.evandev.modest_meals.component.MealContents;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.food.ingredient.MealIngredientManager;
import com.evandev.modest_meals.food.meal.MealType;
import com.evandev.modest_meals.food.meal.MealTypeManager;
import com.evandev.modest_meals.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record MealModelSpec(ResourceLocation mealTypeId, ResourceLocation visualId,
                            @Nullable ResourceLocation portionBase, @Nullable ResourceLocation portionDubious) {

    public static MealModelSpec forItem(Item item) {
        ResourceLocation itemLoc = BuiltInRegistries.ITEM.getKey(item);
        Optional<ModItems.Portion> portion = ModItems.portionOf(item);
        if (portion.isEmpty()) {
            return new MealModelSpec(itemLoc, itemLoc, null, null);
        }
        ResourceLocation wholeLoc = BuiltInRegistries.ITEM.getKey(portion.get().whole().get());
        return new MealModelSpec(wholeLoc, itemLoc,
                itemLoc.withPath(path -> "item/meal/" + path + "/" + path + "_base"),
                itemLoc.withPath(path -> "item/meal/" + path + "/dubious"));
    }

    public Optional<Layers> resolve(ItemStack stack) {
        MealContents contents = stack.get(ModDataComponents.MEAL_CONTENTS.get());
        if (contents == null) {
            return Optional.empty();
        }
        Optional<MealType> maybeType = MealTypeManager.get(mealTypeId);
        if (maybeType.isEmpty()) {
            return Optional.empty();
        }
        MealType type = maybeType.get();

        if (contents.dubious()) {
            return dubiousSprite(type).map(sprite -> new Layers("dubious", List.of(sprite)));
        }
        if (contents.ingredients().isEmpty()) {
            return Optional.empty();
        }
        for (MealType.SpecialSprite special : type.specialSprites()) {
            if (special.matches(contents.ingredients())) {
                return Optional.of(new Layers("special:" + special.sprite(), List.of(special.sprite())));
            }
        }
        if (type.baseSprites().isEmpty()) {
            return Optional.empty();
        }

        MealType.BaseSprites baseSprites = portionBase == null
                ? type.baseSprites().get()
                : new MealType.BaseSprites(Optional.of(portionBase), Optional.empty(), Optional.empty(),
                type.baseSprites().get().repeatIngredients());
        List<ResourceLocation> sprites = new ArrayList<>();
        baseSprites.bottom().ifPresent(sprites::add);
        int slots = baseSprites.getEffectiveLayerSlots(type.maxIngredients());
        for (int i = 0; i < slots; i++) {
            ResourceLocation filling = ingredientSprite(contents.ingredients(), i, slots, type, baseSprites.repeatIngredients());
            if (filling != null) {
                sprites.add(filling);
            }
        }
        baseSprites.top().ifPresent(sprites::add);
        String key = String.join(",", contents.ingredients().stream().map(ResourceLocation::toString).toList());
        return Optional.of(new Layers(key, sprites));
    }

    private @Nullable ResourceLocation ingredientSprite(List<ResourceLocation> ingredients, int index, int totalSlots,
                                                        MealType type, boolean repeat) {
        if (repeat && !ingredients.isEmpty() || index < ingredients.size()) {
            ResourceLocation itemId = ingredients.get(index % ingredients.size());
            Optional<ResourceLocation> sprite = BuiltInRegistries.ITEM.getOptional(itemId)
                    .flatMap(item -> MealIngredientManager.getIngredientSprite(visualId, item, index, totalSlots));
            if (sprite.isPresent()) {
                return sprite.get();
            }
            if (type.requiresSupportedIngredients()) {
                return dubiousSprite(type).orElse(null);
            }
        }
        return null;
    }

    private Optional<ResourceLocation> dubiousSprite(MealType type) {
        if (portionBase == null || type.dubiousSprite().isEmpty()) {
            return type.dubiousSprite();
        }
        return Optional.ofNullable(portionDubious);
    }

    public record Layers(String key, List<ResourceLocation> sprites) {
    }
}
