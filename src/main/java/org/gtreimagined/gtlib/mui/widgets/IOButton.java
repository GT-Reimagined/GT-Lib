package org.gtreimagined.gtlib.mui.widgets;

import brachy.modularui.screen.RichTooltip;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.ToggleButton;
import net.minecraft.client.gui.screens.Screen;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.cover.CoverOutput;
import org.gtreimagined.gtlib.gui.event.GuiEvents;
import org.gtreimagined.gtlib.mui.GTGuiTextures;
import org.gtreimagined.gtlib.util.Utils;
import org.jetbrains.annotations.NotNull;

public class IOButton extends ToggleButton {


    private final boolean item;
    private final PanelSyncManager syncManager;

    public IOButton(boolean item, PanelSyncManager syncManager, BlockEntityMachine<?> machine) {
        this.item = item;
        this.syncManager = syncManager;
        String prefix = item ? "item" : "fluid";
        syncManager.syncValue(prefix + "_output",
                new BooleanSyncValue(() -> machine.coverHandler.map(
                        t -> {
                            if (t.getOutputCover() instanceof CoverOutput coverOutput){
                                return item ? coverOutput.shouldOutputItems() : coverOutput.shouldOutputFluids();
                            }
                            return false;
                        }).orElse(false), b -> {
                    machine.coverHandler.ifPresent(t -> {
                        if (t.getOutputCover() instanceof CoverOutput output) {
                            output.setEjects(item ? output.shouldOutputFluids() : b, item ? b : output.shouldOutputItems());
                        }
                    });
                }).allowC2S());
        GuiEvents event = item ? GuiEvents.ITEM_EJECT : GuiEvents.FLUID_EJECT;
        syncManager.registerSyncedAction(prefix + "_eject", packet -> {
            machine.onGuiEvent(event.factory().apply(event, packet), syncManager.getPlayer());
        });
        this.syncHandler(prefix + "_output");
        this.size(18, 18);
        this.overlay(item ? GTGuiTextures.IO_ITEM : GTGuiTextures.IO_FLUID);
        tooltip(new RichTooltip().add(Utils.translatable("gtlib.tooltip.io_widget." + prefix)));
    }


    @Override
    public @NotNull Result onMousePressed(int button) {
        Result result = super.onMousePressed(button);
        if (result.accepts) syncManager.callSyncedAction((item ? "item" : "fluid") + "_eject", packet -> {
            packet.writeVarIntArray(new int[]{Screen.hasShiftDown() ? 1 : 0, 0});
        });
        return result;
    }

}
