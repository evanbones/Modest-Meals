package com.evandev.modest_meals.trait;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Comparator;
import java.util.List;

public record TagTraitEntry(TagKey<Item> tag, int priority, List<FoodTrait> traits) {

    public static final Comparator<TagTraitEntry> ORDER =
            Comparator.comparingInt(TagTraitEntry::priority).reversed()
                    .thenComparing(entry -> entry.tag().location().toString());

    public static final Codec<TagTraitEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagTraitEntry::tag),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(TagTraitEntry::priority),
            FoodTraitType.CODEC.listOf().fieldOf("traits").forGetter(TagTraitEntry::traits)
    ).apply(instance, TagTraitEntry::new));

    public TagTraitEntry {
        traits = List.copyOf(traits);
    }
}
