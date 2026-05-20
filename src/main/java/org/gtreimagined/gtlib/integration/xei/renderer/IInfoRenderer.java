package org.gtreimagined.gtlib.integration.xei.renderer;

import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.sync.PanelSyncManager;
import org.gtreimagined.gtlib.mui.widgets.GTInfoRenderWidget;
import org.gtreimagined.gtlib.util.int2;

;
;

public interface IInfoRenderer {
    void drawInfo(GTInfoRenderWidget widget, ModularGuiContext context, WidgetThemeEntry<?> widgetTheme);

    void registerSyncHandlers(PanelSyncManager manager);

    int2 getPos();

    int2 getSize();
}
