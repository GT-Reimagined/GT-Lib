package org.gtreimagined.gtlib.texture;

import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.types.Machine;

public interface IOverlayTexturer {
    Texture[] getOverlays(Machine type, MachineState state, Tier tier, int index);
}
