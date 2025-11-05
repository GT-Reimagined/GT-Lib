package org.gtreimagined.gtlib.recipe;

import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;
import org.gtreimagined.gtlib.recipe.map.Proxy;
import org.gtreimagined.gtlib.recipe.map.RecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.gtreimagined.gtlib.registration.GTBiomeModifier;

import java.util.List;
import java.util.function.BiFunction;

public class RecipeProxies {

    private static BiFunction<Recipe<?>, RecipeBuilder, IRecipe> getDefault(int power, int duration) {
        return (t, b) -> {
            List<Ingredient> ingredients = t.getIngredients();
            Ingredient input = ingredients.get(0);
            ItemStack[] stacks = input.getItems();
            RecipeIngredient ing = stacks.length == 1 ? RecipeIngredient.of(stacks[0]) : RecipeIngredient.of(1, input.getItems());
            return b.recipeMapOnly().ii(ing)
                    .io(t.getResultItem(GTBiomeModifier.REGISTRY_ACCESS)).add(t.getId().getPath(), duration, power, 0, 1);
        };
    }

    public static BiFunction<Integer, Integer, Proxy> FURNACE_PROXY = (power, duration) -> new Proxy(RecipeType.SMELTING, getDefault(power, duration));
    public static BiFunction<Integer, Integer, Proxy> BLASTING_PROXY = (power, duration) -> new Proxy(RecipeType.BLASTING, getDefault(power, duration));
    public static BiFunction<Integer, Integer, Proxy> SMOKING_PROXY = (power, duration) -> new Proxy(RecipeType.SMOKING, getDefault(power, duration));
}
