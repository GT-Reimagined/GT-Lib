package muramasa.antimatter.recipe.map;

import muramasa.antimatter.recipe.IRecipe;

import java.util.function.Predicate;
import java.util.function.Supplier;

public record SubCategory(String langKey, Supplier<Object> icon, Predicate<IRecipe> predicate) {
}
