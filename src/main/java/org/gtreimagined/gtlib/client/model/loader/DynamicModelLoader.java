package org.gtreimagined.gtlib.client.model.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.gtreimagined.gtlib.client.IGTModel;
import org.gtreimagined.gtlib.client.model.GTModel;
import org.gtreimagined.gtlib.dynamic.DynamicModel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DynamicModelLoader extends GTModelLoader<DynamicModel> {

        public DynamicModelLoader(ResourceLocation loc) {
            super(loc);
        }

        @NotNull
        @Override
        public DynamicModel read(JsonObject json, JsonDeserializationContext context) {
            try {
                Int2ObjectOpenHashMap<IGTModel[]> configs = new Int2ObjectOpenHashMap<>();
                for (JsonElement e : json.getAsJsonArray("config")) {
                    if (!e.isJsonObject() || !e.getAsJsonObject().has("id") || !e.getAsJsonObject().has("models"))
                        continue;
                    int id = e.getAsJsonObject().get("id").getAsInt();
                    configs.put(id, buildModels(context, e.getAsJsonObject().get("models").getAsJsonArray()));
                }
                String staticMapId = "";
                if (json.has("staticMap") && json.get("staticMap").isJsonPrimitive())
                    staticMapId = json.get("staticMap").getAsString();
                ResourceLocation particle = json.has("particle") ? new ResourceLocation(json.get("particle").getAsString()) : MissingTextureAtlasSprite.getLocation();
                return new DynamicModel(particle, configs, staticMapId);
            } catch (Exception e) {
                throw new RuntimeException("Caught error deserializing model : " + e);
            }
        }

        public IGTModel[] buildModels(JsonDeserializationContext context, JsonArray array) {
            IGTModel[] models = new IGTModel[array.size()];
            for (int i = 0; i < array.size(); i++) {
                if (!array.get(i).isJsonObject()) continue;
                models[i] = new GTModel(context.deserialize(array.get(i).getAsJsonObject(), BlockModel.class), buildRotations(array.get(i).getAsJsonObject()));
            }
            return models;
        }
    }