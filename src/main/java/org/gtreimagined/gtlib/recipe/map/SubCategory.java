package org.gtreimagined.gtlib.recipe.map;

import org.gtreimagined.gtlib.recipe.IRecipe;

import java.util.function.Predicate;
import java.util.function.Supplier;

public record SubCategory(Supplier<Object> icon, Predicate<IRecipe> predicate) {
}
