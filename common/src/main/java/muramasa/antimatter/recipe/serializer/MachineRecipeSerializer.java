package muramasa.antimatter.recipe.serializer;

import muramasa.antimatter.Ref;
import muramasa.antimatter.recipe.Recipe;
import muramasa.antimatter.recipe.ingredient.FluidIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MachineRecipeSerializer extends AntimatterRecipeSerializer<Recipe>{
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
