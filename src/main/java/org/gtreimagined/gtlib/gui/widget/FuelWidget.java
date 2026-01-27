package org.gtreimagined.gtlib.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.blockentity.IFuelMachine;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.Widget;
import org.gtreimagined.gtlib.integration.xei.GTLibXEIPlugin;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.gtreimagined.gtlib.gui.ICanSyncData.SyncDirection.SERVER_TO_CLIENT;

public class FuelWidget extends Widget {
    private int fuel = 0, maxFuel = 0;
    final ResourceLocation base, overlay;
    protected FuelWidget(@NotNull GuiInstance gui, @Nullable IGuiElement parent, ResourceLocation base, ResourceLocation overlay) {
        super(gui, parent);
        this.base = base;
        this.overlay = overlay;
    }

    public static WidgetSupplier build(ResourceLocation base, ResourceLocation overlay) {
        return builder((gui1, parent1) -> new FuelWidget(gui1, parent1, base, overlay));
    }

    @Override
    public void init() {
        super.init();
        if (gui.handler instanceof IFuelMachine fuelMachine){
            gui.syncInt(fuelMachine::getFuel, i -> fuel = i, SERVER_TO_CLIENT);
            gui.syncInt(fuelMachine::getMaxFuel, i -> maxFuel = i, SERVER_TO_CLIENT);
        }
    }

    @Override
    public void render(GuiGraphics graphics, double mouseX, double mouseY, float partialTicks) {
        drawTexture(graphics, base, realX(), realY(), 0, 0, 18, 18, 18, 18);
        if (fuel > 0) {
            float per = (float) fuel / maxFuel;
            if (per > 1.0F) {
                per = 1.0F;
            }
            int lvl = (int) (per * (float) 18);
            if (lvl < 0) {
                return;
            }
            int y = (realY() + 18) - lvl;
            drawTexture(graphics, overlay, realX(), y, 0, 18 - lvl, 18, lvl, 18, 18);
        }
    }

    @Override
    public void mouseOver(GuiGraphics graphics, double mouseX, double mouseY, float partialTicks) {
        super.mouseOver(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics,"Show Recipes", mouseX, mouseY, 0, 0, 18, 18);
        renderTooltip(graphics,"Fuel: " + fuel, mouseX, mouseY + 10, 0, 10, 18, 18);
    }

    @OnlyIn(Dist.CLIENT)
    protected void renderTooltip(GuiGraphics graphics, String text, double mouseX, double mouseY, int x, int y, int w, int h) {
        if (isInside(x, y, w, h, mouseX, mouseY)){
            renderTooltip(graphics, Utils.literal(text), mouseX, mouseY);
        }

    }

    public boolean isInside(int x, int y, int w, int h, double mouseX, double mouseY) {
        int realX = realX() + x;
        int realY = realY() + y;
        return ((mouseX >= realX && mouseX <= realX + w) && (mouseY >= realY && mouseY <= realY + h));
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        super.onClick(mouseX, mouseY, button);
        if (this.gui.handler instanceof BlockEntityMachine<?> machine) {
            GTLibXEIPlugin.showCategory(machine.getMachineType(), machine.getMachineTier());
        }
    }
}
