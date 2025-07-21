package org.gtreimagined.gtlib.integration.kubejs;

import com.google.gson.JsonElement;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;

public class KubeJSRecipe extends RecipeJS {
    @Override
    public InputItem readInputItem(Object from) {
        if (from instanceof JsonElement element){
            from = RecipeIngredient.fromJson(element);
        }
        return super.readInputItem(from);
    }

    @Override
    public InputFluid readInputFluid(Object from) {

        return super.readInputFluid(from);
    }
}
