package org.gtreimagined.gtlib.mui.widgets;

import brachy.modularui.api.value.IValue;
import brachy.modularui.drawable.text.TextRenderer;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.sync.ModularSyncManager;
import brachy.modularui.value.sync.SyncHandler;
import brachy.modularui.widget.Widget;
import net.minecraft.network.chat.Component;
import org.gtreimagined.gtlib.mui.IInfoRenderer;

import java.util.Optional;
import java.util.function.IntSupplier;

public class GTInfoRenderWidget extends Widget<GTInfoRenderWidget> {

    private final IInfoRenderer renderer;

    public GTInfoRenderWidget(IInfoRenderer renderer){
        this.renderer = renderer;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        renderer.drawInfo(this, context, widgetTheme);
    }

    public <T> Optional<T> getSyncedValue(String id, Class<T> tClass){
        ModularSyncManager syncManager = this.getScreen().getSyncManager();
        SyncHandler<?> handler = syncManager.findSyncHandler(id);
        IValue<T> value = handler.castValueNullable(tClass);
        return value == null ? Optional.empty() : Optional.of(value.getValue());
    }

    public void drawText(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme, int x, int y, Component text){
        drawText(context, widgetTheme, x, y, text, Alignment.TopLeft, null, null);
    }

    public void drawText(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme, int x, int y, Component text, int color){
        drawText(context, widgetTheme, x, y, text, Alignment.TopLeft, null, () -> color);
    }

    public void drawText(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme, int x, int y, Component text, Alignment alignment, Boolean shadow, IntSupplier color){
        TextRenderer renderer = TextRenderer.SHARED;
        WidgetTheme theme = getActiveWidgetTheme(widgetTheme, isHovering());
        renderer.setColor(color != null ? color.getAsInt() : theme.getTextColor());
        renderer.setAlignment(alignment, getArea().paddedWidth() + 1, getArea().paddedHeight());
        renderer.setShadow(shadow != null ? shadow : theme.isTextShadow());
        renderer.setPos(getArea().getPadding().left() + x, getArea().getPadding().top() + y);
        renderer.setScale(1);
        renderer.setSimulate(false);
        renderer.draw(context.getGraphics(), text);
    }
}
