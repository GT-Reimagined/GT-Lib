package org.gtreimagined.gtlib.capability;

import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.event.IGuiEvent;
import org.gtreimagined.gtlib.network.packets.AbstractGuiEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public interface IGuiHandler {

    default void onGuiEvent(IGuiEvent event, Player player) {
        // NOOP
    }

    GuiProperties getGuiProperties();

    boolean isRemote();

    default void addWidgets(ModularPanel<?> panel, SidedPosGuiData sidedPosGuiData, PanelSyncManager panelSyncManager, UISettings uiSettings){

    }

    ResourceLocation getGuiTexture();

    default int guiSize() {
        return getGuiProperties().getXSize();
    }

    default int guiHeight() {
        return getGuiProperties().getYSize();
    }

    default int guiTextureSize() {
        return getGuiProperties().getTextureXSize();
    }

    default int guiTextureHeight() {
        return getGuiProperties().getTextureYSize();
    }

    /**
     * Creates a gui packet, depending on the type of gui handler.
     *
     * @param event the event container.
     * @return a packet to send.
     */
    AbstractGuiEventPacket createGuiPacket(IGuiEvent event);

    String handlerDomain();

}
