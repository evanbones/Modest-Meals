package com.evandev.modest_meals.datagen;

import com.evandev.modest_meals.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class ModDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        boolean server = event.includeServer();
        generator.addProvider(server, new MealEffectProvider(output));
        generator.addProvider(server, new MealTypeProvider(output));
        generator.addProvider(server, new MealFormulaProvider(output));
        generator.addProvider(server, new IngredientProfileProvider(output));
        generator.addProvider(server, new MealIngredientProvider(output));
        generator.addProvider(server, new MealRecipeProvider(output));

        generator.addProvider(server, new ModItemTagProvider(output, event.getLookupProvider(),
                CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()),
                event.getExistingFileHelper()));

        generator.addProvider(event.includeClient(),
                new ModItemModelProvider(output, event.getExistingFileHelper()));
    }
}
