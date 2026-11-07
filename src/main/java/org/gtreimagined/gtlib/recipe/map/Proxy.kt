package org.gtreimagined.gtlib.recipe.map

import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeType
import org.gtreimagined.gtlib.recipe.IRecipe
import java.util.function.BiFunction

/**
 * Static classes
 */
@JvmRecord
data class Proxy(@JvmField val loc: RecipeType<*>, @JvmField val handler: BiFunction<Recipe<*>, RecipeBuilder, IRecipe?>)
