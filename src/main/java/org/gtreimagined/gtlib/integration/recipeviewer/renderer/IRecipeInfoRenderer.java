package org.gtreimagined.gtlib.integration.recipeviewer.renderer;

import net.minecraft.network.chat.Component;
import org.gtreimagined.gtlib.recipe.IRecipe;

import java.util.List;

@FunctionalInterface
public interface IRecipeInfoRenderer {
    List<Component> getLines(IRecipe recipe);
}
