package org.gtreimagined.gtlib.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import org.gtreimagined.gtlib.client.model.ProxyModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ProxyModelLoader extends AntimatterModelLoader<ProxyModel> {
    public ProxyModelLoader(ResourceLocation location) {
        super(location);
    }

    @NotNull
    @Override
    public ProxyModel read(JsonDeserializationContext context, JsonObject json) {
        return new ProxyModel();
    }
}