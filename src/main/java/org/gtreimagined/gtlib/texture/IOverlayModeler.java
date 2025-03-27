package org.gtreimagined.gtlib.texture;

import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.types.Machine;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public interface IOverlayModeler {
    ResourceLocation getOverlayModel(Machine<?> type, MachineState state, Direction side);

    IOverlayModeler defaultOverride = (a,s,d) -> new ResourceLocation(a.getDomain(), "block/machine/overlay/" + a.getId() + "/" + d.getSerializedName());
}

