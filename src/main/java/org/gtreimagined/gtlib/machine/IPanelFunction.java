package org.gtreimagined.gtlib.machine;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;

public interface IPanelFunction {
    ModularPanel<?> modifyPanel(ModularPanel<?> modularPanel, PosGuiData guiData, PanelSyncManager syncManager, UISettings settings);
}
