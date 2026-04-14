package org.gtreimagined.gtlib.machine.types;

import lombok.Getter;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityHatch;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.gui.widget.TankIconWidget;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.registration.IColorHandler;
import org.gtreimagined.gtlib.util.Dir;

import static org.gtreimagined.gtlib.machine.MachineFlag.*;

public class HatchMachine extends Machine<HatchMachine> {
    @Getter
    String idForHandlers;

    public HatchMachine(String domain, String id, CoverFactory cover, String idForHandlers) {
        super(domain, id);
        this.idForHandlers = idForHandlers;
        setTile(BlockEntityHatch::new);
        setTiers(Tier.getAllElectric());
        addFlags(HATCH, COVERABLE);
        setVerticalFacingAllowed(true);
        setOutputCover(cover);
        setOutputDir(Dir.FORWARD);
        setAllowsFrontCovers();
        setAllowsOutputCoversOnFacing(true);
        setAllowsFrontIO();
        setBlockColorHandler((state, world, pos, machine, i) -> {
            if (machine instanceof BlockEntityHatch<?> hatch && hatch.getTextureBlock() instanceof IColorHandler colorHandler && i == 0) {
                return colorHandler.getBlockColor(hatch.getTextureBlock().defaultBlockState(), world, pos, i);
            }
            return -1;
        });
    }

    public HatchMachine setIdForHandlers(String idForHandlers) {
        this.idForHandlers = idForHandlers;
        return this;
    }

    @Override
    protected void setupGui() {
        super.setupGui();
        addGuiCallback(t -> {
            if (has(FLUID)){
                t.addWidget(TankIconWidget.build().setPos(8, 39));
            }
        });
    }
}
