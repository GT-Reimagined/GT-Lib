package muramasa.antimatter.recipe.map;

import muramasa.antimatter.recipe.IRecipe;

import java.util.function.Predicate;

public record SubCategory(String langKey, Object icon, Predicate<IRecipe> predicate) {
}
