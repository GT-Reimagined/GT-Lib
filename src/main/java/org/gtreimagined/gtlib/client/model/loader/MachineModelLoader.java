package org.gtreimagined.gtlib.client.model.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.client.model.GTGroupedModel;
import org.gtreimagined.gtlib.client.model.MachineModel;
import org.gtreimagined.gtlib.machine.MachineState;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MachineModelLoader extends GTModelLoader<MachineModel> {

    public MachineModelLoader(ResourceLocation loc) {
        super(loc);
    }

    @NotNull
    @Override
    public MachineModel read(JsonDeserializationContext context, JsonObject json) {
        ResourceLocation particle = json.has("particle") ? new ResourceLocation(json.get("particle").getAsString()) : MissingTextureAtlasSprite.getLocation();
        Map<MachineState, UnbakedModel[]> m = new HashMap<>();
        AntimatterAPI.all(MachineState.class, t -> {
            if (json.has(t.toString().toLowerCase())) {
                JsonArray arr = json.get(t.toString().toLowerCase()).getAsJsonArray();
                UnbakedModel[] a = new UnbakedModel[6];
                for (int i = 0; i < 6; i++) {
                    a[i] = context.deserialize(arr.get(i), BlockModel.class);
                }
                m.put(t, a);
            }
        });
        return new MachineModel(m, particle);
    }

    public static class SideModelLoader extends BlockBenchLoader {
        public SideModelLoader(ResourceLocation loc) {
            super(loc);
        }

        @NotNull
        @Override
        public GTGroupedModel read(JsonDeserializationContext context, JsonObject json) {
            GTGroupedModel model = super.read(context, json);
            return new GTGroupedModel.MachineSideModel(model);
        }
    }

    public static class CoverModelLoader extends BlockBenchLoader {
        public CoverModelLoader(ResourceLocation loc) {
            super(loc);
        }

        @NotNull
        @Override
        public GTGroupedModel read(JsonDeserializationContext context, JsonObject json) {
            GTGroupedModel model = super.read(context, json);
            return new GTGroupedModel.CoverModel(model);
        }
    }
    
}