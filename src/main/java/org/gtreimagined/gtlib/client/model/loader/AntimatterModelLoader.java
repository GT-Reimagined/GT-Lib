package org.gtreimagined.gtlib.client.model.loader;

import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.client.IAntimatterModel;
import net.minecraft.resources.ResourceLocation;

public abstract class AntimatterModelLoader<T extends IAntimatterModel<T>> implements IAntimatterModelLoader<T> {
    private final ResourceLocation loc;

    public AntimatterModelLoader(ResourceLocation loc) {
        this.loc = loc;
        AntimatterAPI.register(IAntimatterModelLoader.class, this);
    }

    public ResourceLocation getLoc() {
        return loc;
    }

    @Override
    public String getId() {
        return getLoc().getPath();
    }
}
