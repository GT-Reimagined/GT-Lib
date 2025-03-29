package org.gtreimagined.gtlib.recipe.serializer;

import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.recipe.Recipe;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MachineRecipeSerializer extends GTRecipeSerializer<Recipe> {
    public static final MachineRecipeSerializer INSTANCE = new MachineRecipeSerializer();

    protected MachineRecipeSerializer() {
        super(Ref.ID, "machine");
    }

    public static void init() {
    }

    @Override
    public RecipeType<Recipe> getRecipeType() {
        return Recipe.RECIPE_TYPE;
    }

    public Recipe createRecipe(@NotNull List<Ingredient> stacksInput, ItemStack[] stacksOutput, @NotNull List<FluidIngredient> fluidsInput, FluidStack[] fluidsOutput, int duration, long power, int special, int amps){
        return new Recipe(stacksInput, stacksOutput, fluidsInput, fluidsOutput, duration, power, special, amps);
    }
}
