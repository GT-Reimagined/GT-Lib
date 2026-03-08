package org.gtreimagined.gtlib.machine;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;

@FunctionalInterface
public interface IPanelFunction {
    void modifyPanel(ModularPanel<?> modularPanel, BlockEntityMachine<?> machine, PosGuiData guiData, PanelSyncManager syncManager, UISettings settings);
}
