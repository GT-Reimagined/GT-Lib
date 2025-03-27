package org.gtreimagined.gtlib.datagen;

import org.gtreimagined.gtlib.datagen.providers.AntimatterRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public interface ICraftingLoader {
    void loadRecipes(Consumer<FinishedRecipe> output, AntimatterRecipeProvider provider);
}
