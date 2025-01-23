package muramasa.antimatter.recipe.serializer;

import com.google.gson.JsonObject;
import muramasa.antimatter.recipe.IRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public interface IAntimatterRecipeSerializer<T extends IRecipe> extends RecipeSerializer<T> {
    void toJson(JsonObject json, IRecipe recipe);

    RecipeType<T> getRecipeType();
}
