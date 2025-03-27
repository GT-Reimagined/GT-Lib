package org.gtreimagined.gtlib.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import org.gtreimagined.gtlib.Antimatter;
import org.gtreimagined.gtlib.client.ModelUtils;
import org.gtreimagined.gtlib.client.model.AntimatterModel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DefaultModelLoader extends AntimatterModelLoader {
    public DefaultModelLoader(ResourceLocation loc) {
        super(loc);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public AntimatterModel read(JsonDeserializationContext context, JsonObject json) {
        try {
            UnbakedModel baseModel = (json.has("model") && json.get("model").isJsonObject()) ? context.deserialize(json.get("model"), BlockModel.class) : ModelUtils.getMissingModel();
            return new AntimatterModel(baseModel, buildRotations(json));
        } catch (Exception e) {
            return onModelLoadingException(e);
        }
    }

    public AntimatterModel onModelLoadingException(Exception e) {
        Antimatter.LOGGER.error("ModelLoader Exception for " + getLoc().toString());
        e.printStackTrace();
        return new AntimatterModel(ModelUtils.getMissingModel());
    }
}