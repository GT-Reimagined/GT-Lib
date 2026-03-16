package org.gtreimagined.gtlib.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.cover.CoverOutput;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.Widget;
import org.gtreimagined.gtlib.gui.container.ContainerMachine;
import org.gtreimagined.gtlib.gui.event.GuiEvents;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import static org.gtreimagined.gtlib.gui.ICanSyncData.SyncDirection.SERVER_TO_CLIENT;
import static org.gtreimagined.gtlib.machine.MachineFlag.FLUID;
import static org.gtreimagined.gtlib.machine.MachineFlag.ITEM;

public class IOWidget extends Widget {

    private boolean hasItem = false;
    private boolean hasFluid = false;
    private boolean itemState = false;
    private boolean fluidState = false;

    protected IOWidget(GuiInstance instance, IGuiElement parent) {
        super(instance, parent);
        this.setX(instance.handler.getGuiProperties().getMachineData().getIoPos().x);
        this.setY(instance.handler.getGuiProperties().getMachineData().getIoPos().y);
        this.setW(36);
        this.setH(18);
        ContainerMachine<?> m = (ContainerMachine<?>) instance.container;
        if (m.getTile().getMachineType().has(ITEM)) {
            hasItem = true;
        }
        if (m.getTile().getMachineType().has(FLUID)) {
            hasFluid = true;
        }
    }

    @Override
    public void mouseOver(GuiGraphics graphics, double mouseX, double mouseY, float partialTicks) {
        super.mouseOver(graphics, mouseX, mouseY, partialTicks);
        if (isInside(0, 0, 18, 18, mouseX, mouseY) && hasFluid){
            renderTooltip(graphics, Utils.translatable("gtlib.tooltip.io_widget.fluid"), mouseX, mouseY);
        } else if (isInside(18, 0, 18, 18, mouseX, mouseY) && hasItem){
            renderTooltip(graphics, Utils.translatable("gtlib.tooltip.io_widget.item"), mouseX, mouseY);
        }
    }

    @Override
    public void render(GuiGraphics graphics, double mouseX, double mouseY, float partialTicks) {
        if (hasItem){
            drawTexture(graphics, new ResourceLocation(Ref.ID, "textures/gui/button/io.png"), realX() + 18, realY(), itemState ? 18 : 0, 18, 18, 18,36, 36);
        }
        if (hasFluid){
            drawTexture(graphics, new ResourceLocation(Ref.ID, "textures/gui/button/io.png"), realX(), realY(), fluidState ? 18 : 0, 0, 18, 18,36, 36);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isEnabled() && isInside(mouseX, mouseY)) {
            boolean clicked = false;
            if (hasItem && isInside(18, 0, 18, 18, mouseX, mouseY)){
                gui.sendPacket(gui.handler.createGuiPacket(new GuiEvents.GuiEvent(GuiEvents.ITEM_EJECT, Screen.hasShiftDown() ? 1 : 0, id)));
                clicked = true;
            }
            if (hasFluid && isInside(0, 0, 18, 18, mouseX, mouseY)){
                gui.sendPacket(gui.handler.createGuiPacket(new GuiEvents.GuiEvent(GuiEvents.FLUID_EJECT, Screen.hasShiftDown() ? 1 : 0, id)));
                clicked = true;
            }
            if (clicked){
                this.clickSound(Minecraft.getInstance().getSoundManager());
                this.onClick(mouseX, mouseY, button);
                return true;
            }
        }
        return false;
    }

    @Override
    public void init() {
        super.init();
        ContainerMachine<?> m = (ContainerMachine<?>) gui.container;
        if (hasItem)
            gui.syncBoolean(() -> (m.getTile().coverHandler.map(t -> ((CoverOutput) t.getOutputCover()).shouldOutputItems()).orElse(false)), this::setItem, SERVER_TO_CLIENT);
        if (hasFluid)
            gui.syncBoolean(() -> (m.getTile().coverHandler.map(t -> ((CoverOutput) t.getOutputCover()).shouldOutputFluids()).orElse(false)), this::setFluid, SERVER_TO_CLIENT);
    }

    private void setItem(boolean item) {
        this.itemState = item;
    }

    private void setFluid(boolean item) {
        this.fluidState = item;
    }

    public static WidgetSupplier build(int x, int y) {
        return builder(IOWidget::new);
    }
}
