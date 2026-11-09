plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.18.2" apply false
    id("net.neoforged.moddev") version "2.0.147" apply false
    id("me.modmuss50.mod-publish-plugin") version "2.2.1" apply false
}

stonecutter active "1.21.1-fabric"

stonecutter parameters {
    val loader = node.metadata.project.substringAfterLast('-')
    val legacy = eval(node.metadata.version, "<26.1")
    constants.match(loader, "fabric", "neoforge")
    constants["farmers_delight"] = legacy || loader == "fabric"
    constants["emi"] = legacy
    constants["thermoo"] = legacy
    constants["shield_expansion"] = legacy
    constants["neoforge_legacy"] = legacy && loader == "neoforge"

    replacements {
        string(eval(node.metadata.version, ">=26.1")) {
            replace("ResourceLocation", "Identifier")
            replace("FMLEnvironment.dist.", "FMLEnvironment.getDist().")
            replace("import net.minecraft.world.item.UseAnim;", "import net.minecraft.world.item.ItemUseAnimation;")
            replace("UseAnim.", "ItemUseAnimation.")
            replace("UseAnim animation", "ItemUseAnimation animation")
            replace("UseAnim useAnimation", "ItemUseAnimation useAnimation")
            replace("public UseAnim getUseAnimation", "public ItemUseAnimation getUseAnimation")
            replace("import net.minecraft.world.level.GameRules;", "import net.minecraft.world.level.gamerules.GameRules;")
            replace("getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)", "getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION)")
            replace("getMinBuildHeight()", "getMinY()")
            replace("MOB_EFFECT.getHolder(", "MOB_EFFECT.get(")
            replace("MOB_EFFECT::getHolder", "MOB_EFFECT::get")
            replace("BuiltInRegistries.ITEM.get(", "BuiltInRegistries.ITEM.getValue(")
            replace("key.location()", "key.identifier()")
            replace("Ingredient.CODEC_NONEMPTY", "Ingredient.CODEC")
            replace("import net.minecraft.Util;", "import net.minecraft.util.Util;")
            replace("MobEffects.MOVEMENT_SPEED", "MobEffects.SPEED")
            replace("VanillaGuiLayers.EXPERIENCE_BAR", "VanillaGuiLayers.CONTEXTUAL_INFO_BAR")
            replace("import net.minecraft.world.level.BlockAndTintGetter;", "import net.minecraft.client.renderer.block.BlockAndTintGetter;")
            replace("method = \"renderHearts\"", "method = \"extractHearts\"")
            replace("Screen.hasShiftDown()", "Minecraft.getInstance().hasShiftDown()")
            replace("import net.minecraft.client.gui.GuiGraphics;", "import net.minecraft.client.gui.GuiGraphicsExtractor;")
            replace("GuiGraphics ", "GuiGraphicsExtractor ")
            replace("context.drawCenteredString(", "context.centeredText(")
            replace("context.drawString(", "context.text(")
            replace("context.renderFakeItem(", "context.fakeItem(")
            replace("context.renderItemDecorations(", "context.itemDecorations(")
            replace("context.renderOutline(", "context.outline(")
            replace("graphics.drawCenteredString(", "graphics.centeredText(")
            replace("graphics.drawString(", "graphics.text(")
            replace("graphics.renderFakeItem(", "graphics.fakeItem(")
            replace("graphics.renderItemDecorations(", "graphics.itemDecorations(")
            replace("graphics.renderOutline(", "graphics.outline(")
            replace("guiGraphics.drawCenteredString(", "guiGraphics.centeredText(")
            replace("guiGraphics.drawString(", "guiGraphics.text(")
            replace("guiGraphics.renderFakeItem(", "guiGraphics.fakeItem(")
            replace("guiGraphics.renderItemDecorations(", "guiGraphics.itemDecorations(")
            replace("guiGraphics.renderOutline(", "guiGraphics.outline(")
        }
        string(eval(node.metadata.version, ">=26.2")) {
            replace("import net.minecraft.advancements.CriteriaTriggers;", "import net.minecraft.advancements.triggers.CriteriaTriggers;")
            replace("minecraft.options.hideGui", "minecraft.gui.hud.isHidden()")
            replace("minecraft.gui.getGuiTicks()", "minecraft.gui.hud.getGuiTicks()")
            replace("minecraft.gui.rightHeight", "minecraft.gui.hud.rightHeight")
            replace(".setScreen(", ".gui.setScreen(")
            replace("minecraft.screen == null", "minecraft.gui.screen() == null")
            replace("import net.minecraft.client.gui.Gui;", "import net.minecraft.client.gui.Hud;")
            replace("Gui.HeartType", "Hud.HeartType")
            replace("@Mixin(Gui.class)", "@Mixin(Hud.class)")
            replace("Gui inGameHud", "Hud inGameHud")
            replace("import net.minecraft.world.entity.EntityType;", "import net.minecraft.world.entity.EntityType;\nimport net.minecraft.world.entity.EntityTypes;")
            replace("EntityType.PLAYER", "EntityTypes.PLAYER")
        }
        string(eval(node.metadata.version, ">=26.3")) {
            replace("PushReaction.DESTROY", "PushReaction.POPPED")
            replace("InputConstants.Type.KEYSYM", "InputConstants.Type.KEYBOARD")
            replace("event.scancode()", "event.keycode()")
        }
    }
}

stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}

for (version in stonecutter.versions.map { it.version }.distinct()) tasks.register("publish$version") {
    group = "publishing"
    dependsOn(stonecutter.tasks.named("publishMods") { metadata.version == version })
}
