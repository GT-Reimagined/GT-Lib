package org.gtreimagined.gtlib.worldgen.feature;

import com.mojang.serialization.Codec;
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

}
