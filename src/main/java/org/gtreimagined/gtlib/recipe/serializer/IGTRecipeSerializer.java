package org.gtreimagined.gtlib.recipe.serializer;

import com.google.gson.JsonObject;
import org.gtreimagined.gtlib.recipe.IRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public interface IGTRecipeSerializer<T extends IRecipe> extends RecipeSerializer<T> {
    void toJson(JsonObject json, IRecipe recipe);

    RecipeType<T> getRecipeType();
}
