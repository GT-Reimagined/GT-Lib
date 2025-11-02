package org.gtreimagined.gtlib.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.cover.ICoverMode;
import org.gtreimagined.gtlib.cover.ICoverModeHandler;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.ICanSyncData;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CoverModeHandlerWidget  extends Widget {
    ICoverMode coverMode;
    public CoverModeHandlerWidget(@NotNull GuiInstance gui, @Nullable IGuiElement parent) {
        super(gui, parent);
    }

    @Override
    public void init() {
        super.init();
        if (gui.handler instanceof ICoverModeHandler coverModeHandler){
            gui.syncInt(coverModeHandler::coverModeToInt, i -> coverMode = coverModeHandler.getCoverMode(i), ICanSyncData.SyncDirection.SERVER_TO_CLIENT);
        }

    }

    @Override
    public void render(GuiGraphics graphics, double mouseX, double mouseY, float partialTicks) {
        if (gui.handler instanceof ICoverModeHandler coverModeHandler && coverMode != null) {
            drawTexture(graphics, gui.handler.getGuiTexture(), realX() + coverMode.getX(), realY() + coverMode.getY(), coverModeHandler.getOverlayX(), coverModeHandler.getOverlayY(), 18, 18);
        }
    }

    public static WidgetSupplier build() {
        return builder(CoverModeHandlerWidget::new);
    }
}
