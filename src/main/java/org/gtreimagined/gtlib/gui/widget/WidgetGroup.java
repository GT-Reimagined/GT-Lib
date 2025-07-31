package org.gtreimagined.gtlib.gui.widget;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class WidgetGroup extends Widget {

    private final List<Widget> children = new ObjectArrayList<>();

    protected WidgetGroup(@NotNull GuiInstance gui, @Nullable IGuiElement parent) {
        super(gui, parent);
    }

    public List<Widget> getChildren() {
        return children;
    }

    @Override
    public void setDepth(int depth) {
        super.setDepth(depth);
        for (Widget child : children) {
            child.setDepth(depth + 1);
        }
    }

    protected void addWidget(WidgetSupplier w) {
        Widget wid = w.buildAndAdd(this.gui, this);
        wid.setDepth(depth() + 1);
        this.children.add(wid);
    }
}
