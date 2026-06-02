package org.gtreimagined.gtlib.mui.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ToggleButton;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.gui.event.GuiEvents;
import org.gtreimagined.gtlib.gui.event.GuiEvents.GuiEvent;
import org.gtreimagined.gtlib.mui.GTGuiTextures;
import org.gtreimagined.gtlib.network.GTLibNetwork;
import org.gtreimagined.gtlib.util.Utils;
import org.jetbrains.annotations.NotNull;

public class IOWidgetItem extends ToggleButton {


    public IOWidgetItem() {
        this.size(18, 18);
        this.overlay(GTGuiTextures.IO_ITEM);
        tooltip(new RichTooltip().add(Utils.translatable("gtlib.tooltip.io_widget.item")));
    }


    @Override
    public @NotNull Result onMousePressed(int button) {
        Result result = super.onMousePressed(button);
        //if (result.accepts) mouseClicked()
        return result;
    }

    public boolean mouseClicked(GuiContext context, int button, IGuiHandler handler) {
        //GTLibNetwork.NETWORK.sendToServer(handler.createGuiPacket(new GuiEvent(GuiEvents.ITEM_EJECT, Interactable.hasShiftDown() ? 1 : 0, 0)));
        return true;
    }

}
