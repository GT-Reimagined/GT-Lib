package org.gtreimagined.gtlib.integration.xei.renderer;

import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.sync.SyncHandler;
import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.gui.widget.InfoRenderWidget;
import net.minecraft.client.gui.Font;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.gtreimagined.gtlib.mui.widgets.GTInfoRenderWidget;

;
;

public interface IInfoRenderer<T extends InfoRenderWidget<T>> {
    /**
     * @param instance
     * @param graphics
     * @param font
     * @param left
     * @param top
     * @return offset that was rendered.
     */
    @OnlyIn(Dist.CLIENT)
    int drawInfo(T instance, GuiGraphics graphics, Font font, int left, int top);

    default void drawInfo(GTInfoRenderWidget widget, ModularGuiContext context, WidgetThemeEntry<?> widgetTheme){

    }

}
