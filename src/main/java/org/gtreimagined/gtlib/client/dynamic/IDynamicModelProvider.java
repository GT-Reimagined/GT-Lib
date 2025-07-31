package org.gtreimagined.gtlib.client.dynamic;

import org.gtreimagined.gtlib.registration.IGTObject;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public interface IDynamicModelProvider extends IGTObject {
    ResourceLocation getModel(String type, Direction dir);
}
