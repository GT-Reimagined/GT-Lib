package org.gtreimagined.gtlib.integration.xei.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.gui.widget.InfoRenderWidget;
import net.minecraft.client.gui.Font;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

;
;

public interface IInfoRenderer<T extends InfoRenderWidget<T>> {
    /**
     * @param instance
     * @param graphics
     * @param font
     * @param left
     * @param top
     * @return offset that was rendered.
     */
    @OnlyIn(Dist.CLIENT)
    int drawInfo(T instance, GuiGraphics graphics, Font font, int left, int top);
}
