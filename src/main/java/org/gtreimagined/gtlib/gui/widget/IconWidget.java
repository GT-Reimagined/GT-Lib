package org.gtreimagined.gtlib.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.Widget;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IconWidget extends Widget {
    ResourceLocation texture;
    protected IconWidget(@NotNull GuiInstance gui, @Nullable IGuiElement parent, ResourceLocation texture) {
        super(gui, parent);
        this.texture = texture;
    }

    @Override
    public void render(PoseStack matrixStack, double mouseX, double mouseY, float partialTicks) {
        drawTexture(matrixStack, texture, realX(), realY(), 0, 0, getW(), getH(), getW(), getH());
    }

    public static WidgetSupplier build(ResourceLocation texture, int x, int y, int width, int height) {
        return builder((a, b) -> new IconWidget(a, b, texture)).setSize(x,y, width, height);
    }
}
