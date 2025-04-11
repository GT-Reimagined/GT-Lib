package org.gtreimagined.gtlib.worldgen.feature;

import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import org.gtreimagined.gtlib.worldgen.IGTWorldgenFunction;
import net.minecraft.world.level.levelgen.feature.Feature;

public interface IGTFeature extends IGTObject, IGTWorldgenFunction {
    Feature<?> asFeature();
}
