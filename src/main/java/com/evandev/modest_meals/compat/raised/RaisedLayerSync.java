package com.evandev.modest_meals.compat.raised;

import dev.yurisuika.raised.client.gui.Layer;
import dev.yurisuika.raised.registry.LayerRegistry;
import dev.yurisuika.raised.util.Configure;
import net.minecraft.resources.ResourceLocation;

public class RaisedLayerSync {
    public static void linkToHotbarIfUnconfigured(ResourceLocation layerId) {
        String key = layerId.toString();
        Layer layer = Configure.getLayers().get(key);

        if (layer == null || !layer.getSync().equals(key)) {
            return;
        }

        Layer linked = LayerRegistry.createLayer(0, 0, Layer.Direction.X.NONE, Layer.Direction.Y.UP, LayerRegistry.HOTBAR);
        Configure.setLayer(key, linked);
    }
}
