package org.gtreimagined.gtlib.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.UnbakedModel;
import org.gtreimagined.gtlib.client.model.PipeFullModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PipeFullModelLoader extends GTModelLoader<PipeFullModel>{
        public PipeFullModelLoader(ResourceLocation location) {
            super(location);
        }

        @NotNull
        @Override
        public PipeFullModel read(JsonObject json, JsonDeserializationContext context) {
            ResourceLocation particle = json.has("particle") ? new ResourceLocation(json.get("particle").getAsString()) : MissingTextureAtlasSprite.getLocation();
            UnbakedModel open = context.deserialize(json.get("open"), BlockModel.class);
            UnbakedModel closed = context.deserialize(json.get("closed"), BlockModel.class);
            return new PipeFullModel(open, closed, particle);
        }
    }