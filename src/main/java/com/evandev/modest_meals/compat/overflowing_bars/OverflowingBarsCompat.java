package com.evandev.modest_meals.compat.overflowing_bars;

import com.evandev.modest_meals.client.hud.RestoredHeartsDrawHelper;
import com.evandev.modest_meals.platform.Platform;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class OverflowingBarsCompat {
    public static final String MOD_ID = "overflowingbars";
    private static final int ROW_HEALTH = 20;

    private static Boolean loaded;

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = Platform.isModLoaded(MOD_ID);
        }
        return loaded;
    }

    public static int healthRows(Player player) {
        return player.getAbsorptionAmount() > 0.0F && player.getMaxHealth() + player.getAbsorptionAmount() > ROW_HEALTH ? 2 : 1;
    }

    public static void renderRestoredHearts(GuiGraphics graphics, Player player, RandomSource random, int posX, int posY, float maxHealth, int currentHealth) {
        RestoredHeartsDrawHelper helper = new RestoredHeartsDrawHelper(player, random);
        int maxHealthPoints = Mth.ceil(maxHealth);
        int hearts = Math.min(10, Mth.ceil(maxHealth / 2.0F));
        int row = Math.min(currentHealth / ROW_HEALTH, Math.max(0, (maxHealthPoints - 1) / ROW_HEALTH));
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 1.0F);
        for (int heart = 0; heart < hearts; heart++) {
            int heartBottom = row * ROW_HEALTH + heart * 2;
            if (heartBottom >= maxHealthPoints) {
                break;
            }
            Pair<RestoredHeartsDrawHelper.RenderedHeart, RestoredHeartsDrawHelper.RenderedHeart> toDraw = helper.heartsToDrawAt(heartBottom + 2);
            if (toDraw.getFirst() == null) {
                continue;
            }
            int x = posX + heart * 8;
            int y = helper.addShakingIfNeeded(posY);
            RestoredHeartsDrawHelper.draw(graphics, toDraw.getFirst(), x, y);
            if (toDraw.getSecond() != null) {
                RestoredHeartsDrawHelper.draw(graphics, toDraw.getSecond(), x, y);
            }
        }
        graphics.pose().popPose();
    }
}
