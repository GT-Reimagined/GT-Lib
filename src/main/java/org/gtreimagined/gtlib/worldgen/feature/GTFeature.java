package org.gtreimagined.gtlib.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.gtreimagined.gtlib.GTAPI;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class GTFeature<F extends FeatureConfiguration> extends Feature<F> implements IGTFeature {

    public GTFeature(Codec<F> codec, Class<?> c) {
        super(codec);
        GTAPI.register(GTFeature.class, this);
        GTAPI.register(IGTFeature.class, this);
    }


    public abstract boolean enabled();

    public abstract void init();

    public Feature<?> asFeature() {
        return this;
    }

    protected Holder<PlacedFeature> getPlacedFeatureFromKey(Registry<PlacedFeature> registry, ResourceKey<PlacedFeature> key) {
        PlacedFeature placedFeature = registry.get(key);
        if (placedFeature == null) return null;
        return Holder.direct(placedFeature);
    }

}
