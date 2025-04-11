package org.gtreimagined.gtlib.worldgen;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.registration.IGTObject;

import java.util.List;

public interface IWorldgenObject<T extends IWorldgenObject<T>> extends IGTObject {
    @Override
    ResourceLocation getLoc();

    String getSubDirectory();

    Codec<T> getCodec();

    List<ResourceKey<Level>> getDimensions();

    default JsonObject toJson() {
        JsonObject json = getCodec().encode((T)this, JsonOps.INSTANCE, new JsonObject()).getOrThrow(false, GTLib.LOGGER::error).getAsJsonObject();
        json.remove("id");
        return json;
    }

    @Override
    default String getId(){
        return getLoc().getPath();
    }

    @Override
    default String getDomain() {
       return getLoc().getNamespace();
    }

    default boolean isBiomeValid(Holder<Biome> biome){
        return true;
    }
}
