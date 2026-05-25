package org.gtreimagined.gtlib.machine.types;

import brachy.modularui.drawable.UITexture;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.gtreimagined.gtlib.blockentity.BlockEntityTank;
import org.gtreimagined.gtlib.machine.IPanelFunction;
import org.gtreimagined.gtlib.mui.IInfoRenderer;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.mui.widgets.GTInfoRenderWidget;
import org.gtreimagined.gtlib.util.Utils;

import java.util.function.Function;

import static org.gtreimagined.gtlib.machine.MachineFlag.*;

@Accessors(chain = true)
public class TankMachine extends Machine<TankMachine> {
    final Function<Tier, Integer> capacityPerTier;

    public TankMachine(String domain, String name) {
        this(domain, name, t -> 8000 * (1 + t.getIntegerId()));
    }

    public TankMachine(String domain, String name, Function<Tier, Integer> capacityPerTier) {
        super(domain, name);
        this.capacityPerTier = capacityPerTier;
        setTile(BlockEntityTank::new);
        addTooltipInfo((machine, stack, world, tooltip, flag) -> {
            tooltip.add(Utils.translatable("machine.tank.capacity", capacityPerTier.apply(machine.getTier())));
        });
        addFlags(ITEM, FLUID, COVERABLE);
        setAllowsFrontCovers();
        setAllowsFrontIO();
    }

    public Function<Tier, Integer> getCapacityPerTier() {
        return capacityPerTier;
    }

    @Override
    protected void setupGui() {
        super.setupGui();
        getGuiFunctions().add(((modularPanel, machine, guiData, syncManager, settings) -> {
            if (machine instanceof IInfoRenderer renderer){
                renderer.registerSyncHandlers(syncManager);
                modularPanel.child(new GTInfoRenderWidget(renderer)
                        .pos(renderer.getPos().x, renderer.getPos().y)
                        .size(renderer.getSize().x, renderer.getSize().y));
            }
        }));
    }

}
