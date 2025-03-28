package org.gtreimagined.gtlib.datagen;

import org.gtreimagined.gtlib.datagen.providers.GTRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public interface ICraftingLoader {
    void loadRecipes(Consumer<FinishedRecipe> output, GTRecipeProvider provider);
}
