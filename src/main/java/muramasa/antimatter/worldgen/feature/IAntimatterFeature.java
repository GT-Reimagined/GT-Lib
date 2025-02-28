package muramasa.antimatter.worldgen.feature;

import muramasa.antimatter.registration.ISharedAntimatterObject;
import muramasa.antimatter.worldgen.IAntimatterWorldgenFunction;
import net.minecraft.world.level.levelgen.feature.Feature;

public interface IAntimatterFeature extends ISharedAntimatterObject, IAntimatterWorldgenFunction {
    Feature<?> asFeature();
}
