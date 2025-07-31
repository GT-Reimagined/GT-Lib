package org.gtreimagined.gtlib.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.Widget;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TankIconWidget extends Widget {
    protected TankIconWidget(@NotNull GuiInstance gui, @Nullable IGuiElement parent) {
        super(gui, parent);
    }

    @Override
    public void render(PoseStack matrixStack, double mouseX, double mouseY, float partialTicks) {
        drawTexture(matrixStack, new ResourceLocation(Ref.ID, "textures/gui/button/tank.png"), realX(), realY(), 0, 0, 18, 18, 18, 18);
    }

    public static WidgetSupplier build() {
        return builder(TankIconWidget::new);
    }
}
