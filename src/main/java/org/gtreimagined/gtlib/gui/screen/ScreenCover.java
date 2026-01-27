package org.gtreimagined.gtlib.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.gui.Widget;
import org.gtreimagined.gtlib.gui.container.ContainerCover;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

//A screen showing the GUI for the cover.
public class ScreenCover<T extends ContainerCover> extends GTContainerScreen<T> implements MenuAccess<T> {

    protected ContainerCover container;
    protected String name;
    protected ResourceLocation gui;

    public ScreenCover(T container, Inventory inv, Component name) {
        super(container, inv, name);
        this.container = container;
        this.gui = container.getCover().getGuiTexture();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        drawTitle(graphics, mouseX, mouseY);
    }

    protected void drawTitle(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(Minecraft.getInstance().font, name, getCenteredStringX(name), 4, 0x404040);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(graphics);
        for (Widget widget : menu.source().widgetsToRender()) {
            if (!widget.isEnabled() || !widget.isVisible()) continue;
            if (widget.depth() >= this.depth()) return;
            widget.render(graphics, mouseX, mouseY, Minecraft.getInstance().getFrameTime());
        }
    }
}
