package org.gtreimagined.gtlib.recipe.map;

import org.gtreimagined.gtlib.recipe.IRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.BiFunction;

/**
 * Static classes
 **/

public record Proxy(RecipeType loc, BiFunction<Recipe<?>, RecipeBuilder, IRecipe> handler) {
}
