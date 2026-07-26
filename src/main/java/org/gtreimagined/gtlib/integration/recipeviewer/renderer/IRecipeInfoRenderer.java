package org.gtreimagined.gtlib.integration.recipeviewer.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.recipe.IRecipe;
import net.minecraft.client.gui.Font;

public interface IRecipeInfoRenderer {
    void render(GuiGraphics stack, IRecipe recipe, Font font, int guiOffsetX, int guiOffsetY);

    default int getRows(){
        return 0;
    }

    default void renderString(GuiGraphics graphics, String string, Font font, float x, float y, int guiOffsetX, int guiOffsetY) {
        renderString(graphics, string, font, x, y, 0xFFFFFF, guiOffsetX, guiOffsetY);
    }

    default void renderString(GuiGraphics graphics, String string, Font font, float x, float y, int color, int guiOffsetX, int guiOffsetY) {
        renderString(graphics, string, font, x, y, color, guiOffsetX, guiOffsetY, true);
    }

    default void renderString(GuiGraphics graphics, String string, Font font, float x, float y, int color, int guiOffsetX, int guiOffsetY, boolean shadow) {
        graphics.drawString(font, string, (guiOffsetX + x), guiOffsetY + y, color, shadow);
    }

    default int stringWidth(String string, Font font) {
        return font.width(string);
    }
}
