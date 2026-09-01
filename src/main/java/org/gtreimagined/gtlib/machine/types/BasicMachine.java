package org.gtreimagined.gtlib.machine.types;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.progress.CompositeProgress;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ProgressWidget;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityMultiMachine;
import org.gtreimagined.gtlib.cover.CoverOutput;
import org.gtreimagined.gtlib.mui.BarDir;
import org.gtreimagined.gtlib.mui.widgets.GTProgressWidget;
import org.gtreimagined.gtlib.mui.widgets.IOButton;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.util.int2;

import static org.gtreimagined.gtlib.machine.MachineFlag.*;

public class BasicMachine extends Machine<BasicMachine> {

    public BasicMachine(String domain, String id) {
        super(domain, id);
        addFlags(BASIC, EU, COVERABLE);
        setTile(BlockEntityMachine::new);
    }

    @Override
    protected void setupGui() {
        super.setupGui();
        guiFunctions.add(((modularPanel, machine, guiData1, syncManager, settings) -> {
            if (has(RECIPE)) {
                if (guiProperties.getMachineData().hasMachineStateWidget()){
                    int2 size = guiProperties.getMachineData().getMachineStateSize();
                    modularPanel.child(new org.gtreimagined.gtlib.mui.widgets.MachineStateWidget(machine.getMachineTier(), this.has(RECIPE), machine::getMachineState,
                            guiProperties.getMachineData().getMachineStateTexture(machine.getMachineTier()))
                            .pos(guiProperties.getMachineData().getMachineStatePos().x, guiProperties.getMachineData().getMachineStatePos().y)
                            .size(size.x, size.y));
                }
                if (guiProperties.getMachineData().hasProgressWidget()){
                    syncManager.syncValue("progress", new DoubleSyncValue(() -> machine.recipeHandler.map(r -> guiProperties.getMachineData().getProgressPercentFunction().apply(r.getCurrentProgress(), r.getMaxProgress())).orElse(0f)));
                    BarDir direction = guiProperties.getMachineData().getDir();
                    UITexture texture = guiProperties.getMachineData().getProgressTexture(machine.getMachineTier());
                    ProgressWidget progressWidget = new GTProgressWidget(machine.getMachineType(), machine.getMachineTier())
                            .tooltip(t -> t.addLine(Utils.translatable("gtlib.gui.show_recipes")))
                            .syncHandler("progress")
                            .pos(guiProperties.getMachineData().getProgressPos().x, guiProperties.getMachineData().getProgressPos().y);
                    modularPanel.child(progressWidget);
                    if (!direction.isCircular()) {
                        progressWidget.texture(texture, direction.toRegularDirection());
                    } else {
                        progressWidget.progress(CompositeProgress.circularLike4Slice(
                                texture.getSubArea(0.0f, 0.0f, 1f, 0.5f),
                                texture.getSubArea(0f, 0.5f,1f, 1f),
                                direction.toCircularDirection()
                        ));
                    }
                }

            }

            if (machine.getOutputFacing() != null &&
                    machine.coverHandler.map(c -> c.getOutputCover() instanceof CoverOutput).orElse(false) &&
                    !(machine instanceof BlockEntityMultiMachine<?>)){
                ParentWidget<?> widget = new ParentWidget<>();
                if (this.has(ITEM)) {
                    widget.child(new IOButton(true, syncManager, machine)
                            .pos(guiProperties.getMachineData().getIoPos().x + 18, guiProperties.getMachineData().getIoPos().y));
                }
                if (this.has(FLUID)) {
                    widget.child(new IOButton(false, syncManager, machine)
                            .pos(guiProperties.getMachineData().getIoPos().x, guiProperties.getMachineData().getIoPos().y));
                }
                modularPanel.child(widget);
            }
        }));
    }
}