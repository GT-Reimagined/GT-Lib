package org.gtreimagined.gtlib.mui.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.PhantomItemSlot;
import org.jetbrains.annotations.Nullable;

public class GTPhantomItemSlot extends PhantomItemSlot implements IGTItemSlot {
    IDrawable overlay;
    IDrawable background = null;


    @Override
    public @Nullable IDrawable getBackground() {
        if (background == null) {
            background = (guiContext, i, i1, i2, i3, widgetTheme) -> {
                IDrawable drawable = GTPhantomItemSlot.super.getBackground();
                if (drawable == null) drawable = widgetTheme.getBackground();
                if (drawable != null) {
                    drawable.draw(guiContext, i, i1, i2, i3, widgetTheme);
                }
                if (overlay != null) {
                    overlay.draw(guiContext, i, i1, i2, i3, widgetTheme);
                }
            };
        }
        return background;
    }

    @Override
    public ItemSlot setDrawable(IDrawable drawable) {
        this.overlay = drawable;
        return this;
    }
}
