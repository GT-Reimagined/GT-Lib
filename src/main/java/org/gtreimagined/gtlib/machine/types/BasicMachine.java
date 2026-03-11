package org.gtreimagined.gtlib.machine.types;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.value.DoubleValue;
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
        addGuiCallback(t -> {
            t.addWidget(WidgetSupplier.build((a, b) -> TextWidget.build(((GTContainerScreen<?>) b).getTitle().getString(), 4210752, false).build(a, b)).setPos(9, 5).clientSide());
            if (has(RECIPE)) {
                t.addWidget(ProgressWidget.build())
                        .addWidget(MachineStateWidget.build());
            }
            if ((has(ITEM) || has(FLUID)))
                t.addWidget(IOWidget.build(9, 63).onlyIf(u -> u.handler instanceof BlockEntityMachine<?> machine &&
                        machine.getOutputFacing() != null &&
                        machine.coverHandler.map(c -> c.getOutputCover() instanceof CoverOutput).orElse(false) &&
                        !(u.handler instanceof BlockEntityMultiMachine<?>)));
        });
        guiFunctions.add(((modularPanel, machine, guiData1, syncManager, settings) -> {
            if (has(RECIPE)) {
                int2 size = guiData.getMachineData().getMachineStateSize();
                UITexture.Builder builder = new UITexture.Builder();
                builder.imageSize(size.x * 2, size.y)
                        .location(guiData.getMachineData().getMachineStateTexture(machine.getMachineTier()));
                modularPanel.child(new org.gtreimagined.gtlib.mui.widgets.MachineStateWidget(machine.getMachineTier(), this.has(RECIPE), machine::getMachineState, builder.build())
                        .pos(guiData.getMachineData().getMachineStatePos().x, guiData.getMachineData().getMachineStatePos().y)
                        .size(size.x, size.y));

                syncManager.syncValue("progress", new DoubleSyncValue(() -> machine.recipeHandler.map(MachineRecipeHandler::getClientProgress).orElse(0f)));
                modularPanel.child(new brachy.modularui.widgets.ProgressWidget()
                        .texture(UITexture
                                .builder()
                                .location(guiData.getMachineData().getProgressTexture(machine.getMachineTier()))
                                .imageSize(guiData.getMachineData().getProgressSize().x, guiData.getMachineData().getProgressSize().y * 2)
                                .build(), guiData.getMachineData().getProgressSize().x)
                        .direction(guiData.getMachineData().getDirection())
                        .syncHandler("progress")
                        .pos(guiData.getMachineData().getProgressPos().x + 6, guiData.getMachineData().getProgressPos().y + 6));
            }

            if (machine.getOutputFacing() != null &&
                    machine.coverHandler.map(c -> c.getOutputCover() instanceof CoverOutput).orElse(false) &&
                    !(machine instanceof BlockEntityMultiMachine<?>)){
                ParentWidget<?> widget = new ParentWidget<>();
                if (this.has(ITEM)) {
                    IOWidgetItem itemWidget = new IOWidgetItem(machine).pos(guiData.getMachineData().getIoPos().x + 18, guiData.getMachineData().getIoPos().y);
                    syncManager.syncValue("item_output",
                            new BooleanSyncValue(() -> machine.coverHandler.map(
                                    t -> ((CoverOutput) t.getOutputCover()).shouldOutputItems()).orElse(false),
                                    itemWidget::setItem));
                    widget.child(itemWidget);
                }
                if (this.has(FLUID)) {
                    IOWidgetFluid fluidWidget = new IOWidgetFluid(machine).pos(guiData.getMachineData().getIoPos().x, guiData.getMachineData().getIoPos().y);
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