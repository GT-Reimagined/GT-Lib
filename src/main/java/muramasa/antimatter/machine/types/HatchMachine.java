package muramasa.antimatter.machine.types;

import lombok.Getter;
import muramasa.antimatter.Data;
import muramasa.antimatter.blockentity.multi.BlockEntityHatch;
import muramasa.antimatter.blockentity.multi.BlockEntityMultiMachine;
import muramasa.antimatter.cover.CoverFactory;
import muramasa.antimatter.cover.ICover;
import muramasa.antimatter.gui.widget.TankIconWidget;
import muramasa.antimatter.machine.Tier;
import muramasa.antimatter.registration.IColorHandler;

import static muramasa.antimatter.machine.MachineFlag.*;

public class HatchMachine extends Machine<HatchMachine> {
    @Getter
    String idForHandlers;

    public HatchMachine(String domain, String id, CoverFactory cover, String idForHandlers) {
        super(domain, id);
        this.idForHandlers = idForHandlers;
        setTile(BlockEntityHatch::new);
        setTiers(Tier.getAllElectric());
        addFlags(HATCH, COVERABLE);
        setGUI(Data.BASIC_MENU_HANDLER);
        setVerticalFacingAllowed(true);
        covers(ICover.emptyFactory, ICover.emptyFactory, cover, ICover.emptyFactory, ICover.emptyFactory, ICover.emptyFactory);
        setOutputCover(cover);
        frontCovers();
        allowFrontIO();
        blockColorHandler((state, world, pos, machine, i) -> {
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
