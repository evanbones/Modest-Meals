package com.evandev.modest_meals;

import com.evandev.modest_meals.client.ClientConfigSetup;
import com.evandev.modest_meals.client.ClientEventHandler;
import com.evandev.modest_meals.component.ModDataComponents;
import com.evandev.modest_meals.config.ModConfig;
import com.evandev.modest_meals.effect.ModMobEffects;
import com.evandev.modest_meals.event.CommonEventHandler;
import com.evandev.modest_meals.network.ModNetworking;
import com.evandev.modest_meals.trait.FoodTraitType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Constants.MOD_ID)
public class ModestMeals {
    public ModestMeals(IEventBus modEventBus, ModContainer modContainer) {
        ModMobEffects.register(modEventBus);
        FoodTraitType.register(modEventBus);
        ModDataComponents.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModNetworking::registerPayloads);

        NeoForge.EVENT_BUS.register(CommonEventHandler.class);

        if (FMLEnvironment.dist.isClient()) {
            ClientConfigSetup.register(modContainer);
            modEventBus.register(ClientEventHandler.class);
            NeoForge.EVENT_BUS.register(ClientEventHandler.GameEvents.class);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModConfig.load();
    }
}
