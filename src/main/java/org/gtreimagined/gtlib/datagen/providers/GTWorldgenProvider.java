package org.gtreimagined.gtlib.datagen.providers;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.gtreimagined.gtlib.datagen.GTLibDynamics;
import org.gtreimagined.gtlib.datagen.IGTLibProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.Map;

public class GTWorldgenProvider implements IGTLibProvider {
    protected final String providerDomain, providerName;
    protected final String subDir;
    protected final Map<ResourceLocation, JsonObject> JSOM_MAP = new Object2ObjectOpenHashMap<>();

    public GTWorldgenProvider(String providerDomain, String providerName, String subDir) {
        this.providerDomain = providerDomain;
        this.providerName = providerName;
        this.subDir = subDir;
    }

    @Override
    public void run() {

    }

    @Override
    public void onCompletion() {
        JSOM_MAP.forEach((r, j) -> {
            GTLibDynamics.RUNTIME_DATA_PACK.addData(fix(r, "worldgen/" + subDir), j.toString().getBytes());
        });
    }
    private static ResourceLocation fix(ResourceLocation identifier, String prefix) {
        return new ResourceLocation(identifier.getNamespace(), prefix + '/' + identifier.getPath() + ".json");
    }

    public void addJsonObject(ResourceLocation id, JsonObject object){
        JSOM_MAP.put(id, object);
    }

    @Override
    public void run(HashCache cache) throws IOException {

    }

    @Override
    public String getName() {
        return null;
    }
}
