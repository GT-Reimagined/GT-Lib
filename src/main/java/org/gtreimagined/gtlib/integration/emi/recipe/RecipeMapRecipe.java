package org.gtreimagined.gtlib.integration.emi.recipe;

import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.gtreimagined.gtlib.integration.emi.GTEMIFluidIngredient;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RecipeMapRecipe implements EmiRecipe {
    private final EmiRecipeCategory category;
    private final IRecipe recipe;
    private final List<EmiIngredient> inputs = new ArrayList<>();
    private final List<EmiStack> outputs = new ArrayList<>();

    public RecipeMapRecipe(EmiRecipeCategory category, IRecipe recipe){
        this.category = category;
        this.recipe = recipe;
        this.recipe.getInputItems().forEach(i -> inputs.add(EmiIngredient.of(i)));
        this.recipe.getInputFluids().forEach(f -> inputs.add(new GTEMIFluidIngredient(f)));
        if (this.recipe.hasOutputItems()){
            for (ItemStack outputItem : this.recipe.getOutputItems(false)) {
                outputs.add(EmiStack.of(outputItem));
            }
        }
        if (recipe.hasOutputFluids()){
            for (FluidStack stack : recipe.getOutputFluids()){
                outputs.add(ForgeEmiStack.of(stack));
            }
        }
    }
    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipe.getId();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 0;
    }

    @Override
    public int getDisplayHeight() {
        return 0;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {

    }
}
