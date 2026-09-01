package org.gtreimagined.gtlib.integration.recipeviewer.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.gtreimagined.gtlib.recipe.IRecipe;
import net.minecraft.client.gui.Font;

import java.util.List;

@FunctionalInterface
public interface IRecipeInfoRenderer {
    List<Component> getLines(IRecipe recipe);
}
