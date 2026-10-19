package com.evandev.modest_meals.compat.raised;

import dev.yurisuika.raised.api.RaisedApi;
import dev.yurisuika.raised.client.gui.layer.Layer;
import dev.yurisuika.raised.client.gui.layer.Layers;
import dev.yurisuika.raised.config.Config;
import net.minecraft.resources.ResourceLocation;

public class RaisedLayerSync {

    public static void registerAndLinkToHotbar(ResourceLocation layerId) {
        RaisedApi.register(layerId, Layer.Position.BOTTOM);

        String key = layerId.toString();
        String hotbar = Layers.HOTBAR.toString();

        boolean configured = Config.getOptions().getGroups().values().stream()
                .anyMatch(group -> group.getLayers().containsKey(key));

        if (configured) {
            return;
        }

        Config.update(options -> options.getGroups().values().forEach(group -> {
            Layer hotbarLayer = group.getLayers().get(hotbar);

            if (hotbarLayer != null) {
                group.getLayers().put(key, Layers.createLayer(hotbarLayer.getPosition()));
            }
        }));
    }
}
