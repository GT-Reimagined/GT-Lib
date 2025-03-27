package org.gtreimagined.gtlib.texture;

import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.types.Machine;

public interface ITextureHandler {
    //Can also return just one texture.
    Texture[] getBase(Machine type, Tier tier, MachineState state);
}
