package org.gtreimagined.gtlib.mui.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Accessors(fluent = true, chain = true)
public class GTFluidSlot extends FluidSlot {
    @Setter
    IDrawable drawable;

    @Override
    public @Nullable IDrawable getBackground() {
        return drawable;
    }
}
