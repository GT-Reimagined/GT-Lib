package org.gtreimagined.gtlib.mui.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.PhantomItemSlot;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

public class GTPhantomItemSlot extends PhantomItemSlot implements IGTItemSlot {
    IDrawable drawable;

    @Override
    public @Nullable IDrawable getBackground() {
        return drawable;
    }

    @Override
    public ItemSlot setDrawable(IDrawable drawable) {
        this.drawable = drawable;
        return this;
    }
}
