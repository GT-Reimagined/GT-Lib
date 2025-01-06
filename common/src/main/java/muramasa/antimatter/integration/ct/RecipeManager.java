package muramasa.antimatter.integration.ct;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import muramasa.antimatter.recipe.IRecipe;
import muramasa.antimatter.recipe.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.antimatter.Machines")
public class RecipeManager implements IRecipeManager<IRecipe> {

    @Override
    public RecipeType<IRecipe> getRecipeType() {
        return Recipe.RECIPE_TYPE;
    }

    @ZenCodeType.Method
    public CTRecipeBuilder recipeBuilder(String mapId){
        return new CTRecipeBuilder(mapId, this);
    }
}