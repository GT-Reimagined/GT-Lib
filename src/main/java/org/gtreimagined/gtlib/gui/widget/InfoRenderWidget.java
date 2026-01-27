package org.gtreimagined.gtlib.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityMultiMachine;
import org.gtreimagined.gtlib.capability.machine.MachineRecipeHandler;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.Widget;
import org.gtreimagined.gtlib.integration.xei.renderer.IInfoRenderer;
import net.minecraft.client.Minecraft;

import static org.gtreimagined.gtlib.gui.ICanSyncData.SyncDirection.SERVER_TO_CLIENT;

public class InfoRenderWidget<T extends InfoRenderWidget<T>> extends Widget {

    final IInfoRenderer<T> renderer;

    protected InfoRenderWidget(GuiInstance gui, IGuiElement parent, IInfoRenderer<T> renderer) {
        super(gui, parent);
        this.renderer = renderer;
    }

    @Override
    public void render(GuiGraphics graphics, double mouseX, double mouseY, float partialTicks) {
        renderer.drawInfo((T) this, graphics, Minecraft.getInstance().font, realX(), realY());
    }

    public static <T extends InfoRenderWidget<T>> WidgetSupplier build(IInfoRenderer<T> renderer) {
        return builder((a, b) -> new InfoRenderWidget<>(a, b, renderer));
    }

    public static WidgetSupplier build() {
        return builder((a, b) -> new InfoRenderWidget<>(a, b, (IInfoRenderer<?>) a.handler));
    }

    public static class MultiRenderWidget extends InfoRenderWidget<MultiRenderWidget> {

        public int currentProgress = 0;
        public int maxProgress = 0;
        public int overclock = 0;
        public long euT = 0;

        protected MultiRenderWidget(GuiInstance gui, IGuiElement parent, IInfoRenderer<MultiRenderWidget> renderer) {
            super(gui, parent, renderer);
        }

        public boolean drawActiveInfo() {
            return true;
        }

        @Override
        public void init() {
            super.init();
            BlockEntityMultiMachine<?> m = (BlockEntityMultiMachine<?>) gui.handler;
            gui.syncInt(() -> m.recipeHandler.map(MachineRecipeHandler::getCurrentProgress).orElse(0), i -> this.currentProgress = i, SERVER_TO_CLIENT);
            gui.syncInt(() -> m.recipeHandler.map(MachineRecipeHandler::getMaxProgress).orElse(0), i -> this.maxProgress = i, SERVER_TO_CLIENT);
            gui.syncInt(() -> m.recipeHandler.map(MachineRecipeHandler::getOverclock).orElse(0), i -> this.overclock = i, SERVER_TO_CLIENT);
            gui.syncLong(() -> m.recipeHandler.map(MachineRecipeHandler::getPower).orElse(0L), i -> this.euT = i, SERVER_TO_CLIENT);
        }

        public static WidgetSupplier build() {
            return builder((a, b) -> new MultiRenderWidget(a, b, (IInfoRenderer) a.handler));
        }
    }
}
