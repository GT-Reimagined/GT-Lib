package org.gtreimagined.gtlib.client.dynamic;

import org.gtreimagined.gtlib.registration.IAntimatterObject;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public interface IDynamicModelProvider extends IAntimatterObject {
    ResourceLocation getModel(String type, Direction dir);
}
