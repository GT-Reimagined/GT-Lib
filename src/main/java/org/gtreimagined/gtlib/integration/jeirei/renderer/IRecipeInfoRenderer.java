package org.gtreimagined.gtlib.integration.jeirei.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.gtreimagined.gtlib.recipe.IRecipe;
import net.minecraft.client.gui.Font;

public interface IRecipeInfoRenderer {
    void render(PoseStack stack, IRecipe recipe, Font fontRenderer, int guiOffsetX, int guiOffsetY);

    default int getRows(){
        return 0;
    }

    default void renderString(PoseStack stack, String string, Font render, float x, float y, int guiOffsetX, int guiOffsetY) {
        renderString(stack, string, render, x, y, 0xFFFFFF, guiOffsetX, guiOffsetY);
    }

    default void renderString(PoseStack stack, String string, Font render, float x, float y, int color, int guiOffsetX, int guiOffsetY) {
        renderString(stack, string, render, x, y, color, guiOffsetX, guiOffsetY, true);
    }

    default void renderString(PoseStack stack, String string, Font render, float x, float y, int color, int guiOffsetX, int guiOffsetY, boolean shadow) {
        if (shadow) {
            render.drawShadow(stack, string, (guiOffsetX + x), guiOffsetY + y, color);
        } else {
            render.draw(stack, string, (guiOffsetX + x), guiOffsetY + y, color);
        }
    }

    default int stringWidth(String string, Font renderer) {
        return renderer.width(string);
    }
}
