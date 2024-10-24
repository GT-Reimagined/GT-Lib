package muramasa.antimatter.integration.jeirei.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import muramasa.antimatter.gui.widget.InfoRenderWidget;
import net.minecraft.client.gui.Font;
import xyz.wagyourtail.unimined.expect.annotation.Environment;

;
;

public interface IInfoRenderer<T extends InfoRenderWidget<T>> {
    /**
     * @param instance
     * @param stack
     * @param renderer
     * @param left
     * @param top
     * @return offset that was rendered.
     */
    @Environment(Environment.EnvType.CLIENT)
    int drawInfo(T instance, PoseStack stack, Font renderer, int left, int top);
}
