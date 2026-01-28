package org.gtreimagined.gtlib.client.model.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.client.model.PipeModelNew;

public class PipeModelNewLoader extends GTModelLoader<PipeModelNew>{
    public PipeModelNewLoader(ResourceLocation loc) {
        super(loc);
    }

    @Override
    public PipeModelNew read(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation particle = json.has("particle") ? new ResourceLocation(json.get("particle").getAsString()) : MissingTextureAtlasSprite.getLocation();
        UnbakedModel base = context.deserialize(json.get("base"), BlockModel.class);
        UnbakedModel baseEnd = context.deserialize(json.get("base_end"), BlockModel.class);
        UnbakedModel[] connections = new UnbakedModel[6];
        UnbakedModel[] connectionsEnd = new UnbakedModel[6];
        JsonArray connectionsJson = json.getAsJsonArray("connections");
        JsonArray connectionsJsonEnd = json.getAsJsonArray("connections_end");
        for (Direction dir : Direction.values()) {
            connections[dir.get3DDataValue()] = context.deserialize(connectionsJson.get(dir.get3DDataValue()), BlockModel.class);
            connectionsEnd[dir.get3DDataValue()] = context.deserialize(connectionsJsonEnd.get(dir.get3DDataValue()), BlockModel.class);
        }
        return new PipeModelNew(base, baseEnd, connections, connectionsEnd, particle);
    }
}
