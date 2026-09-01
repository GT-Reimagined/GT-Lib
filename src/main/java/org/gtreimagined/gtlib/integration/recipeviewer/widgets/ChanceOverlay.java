package org.gtreimagined.gtlib.integration.recipeviewer.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public record ChanceOverlay(Component text) implements IDrawable {
    @Override
    public void draw(GuiContext guiContext, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiGraphics graphics = guiContext.getGraphics();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        graphics.pose().scale(0.5f, 0.5f, 1);
        graphics.drawString(Minecraft.getInstance().font, text, x+1, y+1, 0xFFFF00, true);

        graphics.pose().popPose();
    }
}
