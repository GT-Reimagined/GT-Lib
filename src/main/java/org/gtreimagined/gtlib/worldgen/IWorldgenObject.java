package org.gtreimagined.gtlib.worldgen;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.registration.IGTObject;

public interface IWorldgenObject extends IGTObject {
    @Override
    ResourceLocation getLoc();

    String getSubDirectory();

    JsonObject toJson();

    @Override
    default String getId(){
        return getLoc().getPath();
    }

    @Override
    default String getDomain() {
       return getLoc().getNamespace();
    }
}
