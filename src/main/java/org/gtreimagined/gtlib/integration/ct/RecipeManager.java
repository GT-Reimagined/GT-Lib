package org.gtreimagined.gtlib.integration.ct;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import org.gtreimagined.gtlib.recipe.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.gtlib.MachineRecipeManager")
public class RecipeManager implements IRecipeManager<Recipe> {

    @Override
    public RecipeType<Recipe> getRecipeType() {
        return Recipe.RECIPE_TYPE;
    }

    @ZenCodeType.Method
    public CTRecipeBuilder recipeBuilder(String mapId){
        return new CTRecipeBuilder(mapId, this);
    }
}