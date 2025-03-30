package org.gtreimagined.gtlib.worldgen.feature;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.worldgen.object.WorldGenBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;

public abstract class GTFeature<F extends FeatureConfiguration> extends Feature<F> implements IGTFeature {

    Object2ObjectMap<ResourceLocation, List<WorldGenBase<?>>> REGISTRY = new Object2ObjectOpenHashMap<>();

    public GTFeature(Codec<F> codec, Class<?> c) {
        super(codec);
        GTAPI.register(GTFeature.class, c.getName(), getDomain(), this);
        GTAPI.register(IGTFeature.class, c.getName(), getDomain(), this);
        //this.setRegistryName(new ResourceLocation(getDomain(), getId()));
    }


    public abstract boolean enabled();

    public void onDataOverride(JsonObject json) {
        getRegistry().values().forEach(list -> list.forEach(base -> base.onDataOverride(json)));
    }

    public abstract void init();

    public Object2ObjectMap<ResourceLocation, List<WorldGenBase<?>>> getRegistry() {
        return REGISTRY;
    }

    public Feature<?> asFeature() {
        return this;
    }
}
