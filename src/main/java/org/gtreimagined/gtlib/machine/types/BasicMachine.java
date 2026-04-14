package org.gtreimagined.gtlib.machine.types;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.widget.ParentWidget;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityMultiMachine;
import org.gtreimagined.gtlib.capability.machine.MachineRecipeHandler;
import org.gtreimagined.gtlib.cover.CoverOutput;
import org.gtreimagined.gtlib.gui.screen.GTContainerScreen;
import org.gtreimagined.gtlib.gui.widget.IOWidget;
import org.gtreimagined.gtlib.gui.widget.MachineStateWidget;
import org.gtreimagined.gtlib.gui.widget.ProgressWidget;
import org.gtreimagined.gtlib.gui.widget.TextWidget;
import org.gtreimagined.gtlib.gui.widget.WidgetSupplier;
import org.gtreimagined.gtlib.mui.widgets.GTProgressWidget;
import org.gtreimagined.gtlib.mui.widgets.IOWidgetFluid;
import org.gtreimagined.gtlib.mui.widgets.IOWidgetItem;
import org.gtreimagined.gtlib.util.int2;

import static org.gtreimagined.gtlib.machine.MachineFlag.*;

public class BasicMachine extends Machine<BasicMachine> {

    public BasicMachine(String domain, String id) {
        super(domain, id);
        addFlags(BASIC, EU, COVERABLE);
        setTile(BlockEntityMachine::new);
        setGUI(Data.BASIC_MENU_HANDLER);
    }

    @Override
    protected void setupGui() {
        super.setupGui();
        guiFunctions.add(((modularPanel, machine, guiData1, syncManager, settings) -> {
            if (has(RECIPE)) {
                int2 size = guiProperties.getMachineData().getMachineStateSize();
                modularPanel.child(new org.gtreimagined.gtlib.mui.widgets.MachineStateWidget(machine.getMachineTier(), this.has(RECIPE), machine::getMachineState,
                        guiProperties.getMachineData().getMachineStateTexture(machine.getMachineTier()))
                        .pos(guiProperties.getMachineData().getMachineStatePos().x, guiProperties.getMachineData().getMachineStatePos().y)
                        .size(size.x, size.y));

                syncManager.syncValue("progress", new DoubleSyncValue(() -> machine.recipeHandler.map(r -> guiProperties.getMachineData().getProgressPercentFunction().apply(r.getCurrentProgress(), r.getMaxProgress())).orElse(0f)));
                modularPanel.child(new GTProgressWidget(machine.getMachineType(), machine.getMachineTier())
                        .texture(guiProperties.getMachineData().getProgressTexture(machine.getMachineTier()), guiProperties.getMachineData().getProgressSize().x)
                        .direction(guiProperties.getMachineData().getDirection())
                        .syncHandler("progress")
                        .pos(guiProperties.getMachineData().getProgressPos().x + 6, guiProperties.getMachineData().getProgressPos().y + 6));
            }

            if (machine.getOutputFacing() != null &&
                    machine.coverHandler.map(c -> c.getOutputCover() instanceof CoverOutput).orElse(false) &&
                    !(machine instanceof BlockEntityMultiMachine<?>)){
                ParentWidget<?> widget = new ParentWidget<>();
                if (this.has(ITEM)) {
                    IOWidgetItem itemWidget = new IOWidgetItem(machine).pos(guiProperties.getMachineData().getIoPos().x + 18, guiProperties.getMachineData().getIoPos().y);
                    syncManager.syncValue("item_output",
                            new BooleanSyncValue(() -> machine.coverHandler.map(
                                    t -> ((CoverOutput) t.getOutputCover()).shouldOutputItems()).orElse(false),
                                    itemWidget::setItem));
                    widget.child(itemWidget);
                }
                if (this.has(FLUID)) {
                    IOWidgetFluid fluidWidget = new IOWidgetFluid(machine).pos(guiProperties.getMachineData().getIoPos().x, guiProperties.getMachineData().getIoPos().y);
                    syncManager.syncValue("fluid_output",
                            new BooleanSyncValue(() -> machine.coverHandler.map(
                                    t -> ((CoverOutput) t.getOutputCover()).shouldOutputFluids()).orElse(false),
                                    fluidWidget::setFluid));
                    widget.child(fluidWidget);
                }
                modularPanel.child(widget);
            }
        }));
    }
}