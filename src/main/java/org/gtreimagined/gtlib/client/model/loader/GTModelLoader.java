package org.gtreimagined.gtlib.client.model.loader;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.client.IGTModel;
import net.minecraft.resources.ResourceLocation;

public abstract class GTModelLoader<T extends IGTModel<T>> implements IGTModelLoader<T> {
    private final ResourceLocation loc;

    public GTModelLoader(ResourceLocation loc) {
        this.loc = loc;
        GTAPI.register(IGTModelLoader.class, this);
    }

    public ResourceLocation getLoc() {
        return loc;
    }

    @Override
    public String getId() {
        return getLoc().getPath();
    }
}
