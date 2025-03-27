package org.gtreimagined.gtlib.worldgen.feature;

import org.gtreimagined.gtlib.registration.ISharedAntimatterObject;
import org.gtreimagined.gtlib.worldgen.IAntimatterWorldgenFunction;
import net.minecraft.world.level.levelgen.feature.Feature;

public interface IAntimatterFeature extends ISharedAntimatterObject, IAntimatterWorldgenFunction {
    Feature<?> asFeature();
}
