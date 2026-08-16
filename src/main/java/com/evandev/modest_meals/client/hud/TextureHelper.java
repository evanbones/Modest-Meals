package com.evandev.modest_meals.client.hud;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class TextureHelper {
    private static final ResourceLocation HEART_FULL_TEXTURE_PATH = ResourceLocation.withDefaultNamespace(
            "textures/gui/sprites/hud/heart/full.png"
    );
    private static final ResourceLocation HEART_HALF_TEXTURE_PATH = ResourceLocation.withDefaultNamespace(
            "textures/gui/sprites/hud/heart/half.png"
    );
    private static final ResourceLocation HEART_FULL_BLINKING_TEXTURE_PATH = ResourceLocation.withDefaultNamespace(
            "textures/gui/sprites/hud/heart/full_blinking.png"
    );

    private final Minecraft minecraft;

    public TextureHelper() {
        this.minecraft = Minecraft.getInstance();
    }

    protected boolean checkIfPixelOpaque(int abgr) {
        int alpha = (abgr >> 24) & 0xFF;
        return alpha > 0;
    }

    private void generateHeartTexture(
            ResourceLocation inputTexture, ResourceLocation outputTexture, boolean overwriteWithWhite, ImagePixelColorChecker checker
    ) throws IOException {
        Optional<Resource> heartResource = minecraft.getResourceManager().getResource(inputTexture);
        if (heartResource.isEmpty()) {
            return;
        }
        try (InputStream stream = heartResource.get().open(); NativeImage image = NativeImage.read(stream)) {
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int abgr = image.getPixelRGBA(x, y);
                    int color;
                    if (!checker.shouldBeOpaque(abgr, x, y)) {
                        color = 0; // fully transparent
                    } else if (overwriteWithWhite) {
                        color = -1; // fully opaque white
                    } else {
                        color = abgr;
                    }

                    image.setPixelRGBA(x, y, color);
                }
            }
            DynamicTexture dynamicTexture = new DynamicTexture(image);
            minecraft.getTextureManager().register(outputTexture, dynamicTexture);
        }
    }

    public void generateHeartTextures() throws IOException {
        generateHeartTexture(
                HEART_FULL_TEXTURE_PATH, RestoredHeartsDrawHelper.WHITE_FULL_HEART_TEXTURE, true, (abgr, x, y) -> checkIfPixelOpaque(abgr)
        );
        generateHeartTexture(
                HEART_HALF_TEXTURE_PATH, RestoredHeartsDrawHelper.WHITE_HALF_HEART_TEXTURE, true, (abgr, x, y) -> checkIfPixelOpaque(abgr)
        );
        try (var checker = new RightHalfHeartPixelColorChecker()) {
            generateHeartTexture(HEART_FULL_TEXTURE_PATH, RestoredHeartsDrawHelper.WHITE_RIGHT_HALF_HEART_TEXTURE, true, checker);
            generateHeartTexture(HEART_FULL_TEXTURE_PATH, RestoredHeartsDrawHelper.ORIGINAL_RIGHT_HALF_HEART_TEXTURE, false, checker);
            generateHeartTexture(HEART_FULL_BLINKING_TEXTURE_PATH, RestoredHeartsDrawHelper.BLINKING_RIGHT_HALF_HEART_TEXTURE, false, checker);
        }
    }

    @FunctionalInterface
    private interface ImagePixelColorChecker {
        boolean shouldBeOpaque(int abgr, int x, int y);
    }

    private class RightHalfHeartPixelColorChecker implements ImagePixelColorChecker, AutoCloseable {
        private final NativeImage halfTexture;

        private RightHalfHeartPixelColorChecker() throws IOException {
            Optional<Resource> heartResource = minecraft.getResourceManager().getResource(HEART_HALF_TEXTURE_PATH);
            try (InputStream stream = heartResource.orElseThrow().open()) {
                halfTexture = NativeImage.read(stream);
            }
        }

        @Override
        public boolean shouldBeOpaque(int abgr, int x, int y) {
            // full heart pixel is opaque and half heart pixel is transparent - this pixel only exists on the right half
            return checkIfPixelOpaque(abgr) && !checkIfPixelOpaque(halfTexture.getPixelRGBA(x, y));
        }

        @Override
        public void close() {
            halfTexture.close();
        }
    }
}
