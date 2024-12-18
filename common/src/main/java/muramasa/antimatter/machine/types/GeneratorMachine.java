package muramasa.antimatter.machine.types;

import muramasa.antimatter.Data;
import muramasa.antimatter.blockentity.single.BlockEntityGenerator;
import muramasa.antimatter.util.Utils;

import static muramasa.antimatter.machine.MachineFlag.*;

public class GeneratorMachine extends Machine<GeneratorMachine> {
    public GeneratorMachine(String domain, String id) {
        super(domain, id);
        addFlags(BASIC, EU, COVERABLE, GENERATOR);
        setTile(BlockEntityGenerator::new);
        setGUI(Data.BASIC_MENU_HANDLER);
        noCovers();
        setVerticalFacingAllowed(true);
        addTooltipInfo((machine, stack, world, tooltip, flag) -> tooltip.add(Utils.translatable("machine.generator.efficiency", this.getMachineEfficiency(machine.getTier()) + "%")));
    }
}
