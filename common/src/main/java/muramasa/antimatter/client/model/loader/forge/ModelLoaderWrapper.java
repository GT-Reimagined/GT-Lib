package muramasa.antimatter.client.model.loader.forge;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import muramasa.antimatter.client.IAntimatterModel;
import muramasa.antimatter.client.model.loader.IAntimatterModelLoader;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public record ModelLoaderWrapper(IAntimatterModelLoader<?> loader) implements IModelLoader {
    @Override
    public IModelGeometry read(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
        return loader.readModel(jsonDeserializationContext, jsonObject);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {

    }
}
