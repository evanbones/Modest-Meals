package com.evandev.modest_meals.food;

import com.google.gson.JsonElement;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;

//? if >=26.1 {
/*import net.minecraft.resources.FileToIdConverter;
import net.minecraft.util.ExtraCodecs;
*///?} else {
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//?}

//? if >=26.1 {
/*public abstract class JsonReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
*///?} else {
public abstract class JsonReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
//?}

    protected JsonReloadListener(String directory) {
        //? if >=26.1 {
        /*super(ExtraCodecs.JSON, FileToIdConverter.json(directory));
        *///?} else
        super(GSON, directory);
    }

    //? if neoforge {
    protected ItemTagLookup tagLookup() {
        return ItemTagLookup.of(this.getContext());
    }
    //?} else {
    /*protected ItemTagLookup tagLookup() {
        return ItemTagLookup.UNKNOWN;
    }
    *///?}
}
