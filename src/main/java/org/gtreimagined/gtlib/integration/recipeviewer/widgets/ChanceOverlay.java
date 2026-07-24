package org.gtreimagined.gtlib.integration.recipeviewer.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public record ChanceOverlay(Component text, int x, int y) implements IDrawable {
    @Override
    public void draw(GuiContext guiContext, int i, int i1, int i2, int i3, WidgetTheme widgetTheme) {
        GuiGraphics graphics = guiContext.getGraphics();
        graphics.pose().pushPose();
        graphics.pose().scale(0.5f, 0.5f, 1);
        graphics.drawString(Minecraft.getInstance().font, text, 2*x, 2*y, 0xFFFF00, true);

        graphics.pose().popPose();
    }
}
