package org.gtreimagined.gtlib.gui.widget;

import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.ICanSyncData;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class SyncableTextWidget extends TextWidget {
    String text = "";
    final Function<IGuiHandler, String> syncFunction;
    protected SyncableTextWidget(@NotNull GuiInstance gui, @Nullable IGuiElement parent, Function<IGuiHandler, String> syncFunction, int color, boolean centered){
        super(gui, parent, a-> ((SyncableTextWidget)a).text, color, centered);
        this.syncFunction = syncFunction;

    }

    public static WidgetSupplier build(Function<IGuiHandler, String> textSyncFunction, int color, boolean centered) {
        return builder((a, b) -> new SyncableTextWidget(a, b, textSyncFunction, color, centered));
    }

    @Override
    public void init() {
        super.init();
        gui.syncString(() -> syncFunction.apply(gui.handler), s -> text = s, ICanSyncData.SyncDirection.SERVER_TO_CLIENT);
    }
}
