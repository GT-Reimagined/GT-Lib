package org.gtreimagined.gtlib.capability;

import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.gui.event.IGuiEvent;

public interface IGuiHandler {

    default void onGuiEvent(IGuiEvent event, Player player) {
        // NOOP
    }

    GuiProperties getGuiProperties();

    boolean isRemote();

    default void addWidgets(ModularPanel<?> panel, SidedPosGuiData sidedPosGuiData, PanelSyncManager panelSyncManager, UISettings uiSettings){

    }

}
