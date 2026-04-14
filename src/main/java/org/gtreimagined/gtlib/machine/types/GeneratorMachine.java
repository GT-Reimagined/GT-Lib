package org.gtreimagined.gtlib.machine.types;

import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.blockentity.single.BlockEntityGenerator;
import org.gtreimagined.gtlib.util.Utils;

import static org.gtreimagined.gtlib.machine.MachineFlag.*;

public class GeneratorMachine extends Machine<GeneratorMachine> {
    public GeneratorMachine(String domain, String id) {
        super(domain, id);
        addFlags(BASIC, EU, COVERABLE, GENERATOR);
        setTile(BlockEntityGenerator::new);
        setNoOutputCover();
        setVerticalFacingAllowed(true);
        addTooltipInfo((machine, stack, world, tooltip, flag) -> tooltip.add(Utils.translatable("machine.generator.efficiency", this.getMachineEfficiency(machine.getTier()) + "%")));
    }
}
