package org.gtreimagined.gtlib.mui.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.Interactable;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ButtonWidget;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.gui.event.GuiEvents;
import org.gtreimagined.gtlib.gui.event.GuiEvents.GuiEvent;
import org.gtreimagined.gtlib.mui.GTGuiTextures;
import org.gtreimagined.gtlib.network.GTLibNetwork;
import org.gtreimagined.gtlib.util.Utils;

public class IOWidgetFluid extends ButtonWidget<IOWidgetFluid> {

    private boolean fluidState = false;
    private IDrawable fluidOff, fluidOn;

    public IOWidgetFluid(IGuiHandler handler) {
        this.size(18, 18);
        fluidOff = GTGuiTextures.IO_BUTTON.getSubArea(0, 0.5f, 0.5f, 1);
        fluidOn = GTGuiTextures.IO_BUTTON.getSubArea(0.5f, 0.5f, 1, 1);
        tooltip(new RichTooltip().add(Utils.translatable("gtlib.tooltip.io_widget.fluid")));
        this.onMousePressed((mouseX, mouseY, button) -> this.mouseClicked(mouseX, mouseY, button, handler));
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        IDrawable draw = fluidState ? fluidOn : fluidOff;
        draw.drawAtZero(context, this.getArea().getWidth(), this.getArea().getHeight(), widgetTheme.getTheme(false));
    }



    public boolean mouseClicked(double mouseX, double mouseY, int button, IGuiHandler handler) {
        GTLibNetwork.NETWORK.sendToServer(handler.createGuiPacket(new GuiEvent(GuiEvents.FLUID_EJECT, Interactable.hasShiftDown() ? 1 : 0, 0)));
        return true;
    }

    /*
    @Override
    public void init() {
        super.init();
        ContainerMachine<?> m = (ContainerMachine<?>) gui.container;
        if (hasItem)
            gui.syncBoolean(() -> (m.getTile().coverHandler.map(t -> ((CoverOutput) t.getOutputCover()).shouldOutputItems()).orElse(false)), this::setItem, SERVER_TO_CLIENT);
        if (hasFluid)
            gui.syncBoolean(() -> (m.getTile().coverHandler.map(t -> ((CoverOutput) t.getOutputCover()).shouldOutputFluids()).orElse(false)), this::setFluid, SERVER_TO_CLIENT);
    }*/

    private void setFluid(boolean item) {
        this.fluidState = item;
    }

}
