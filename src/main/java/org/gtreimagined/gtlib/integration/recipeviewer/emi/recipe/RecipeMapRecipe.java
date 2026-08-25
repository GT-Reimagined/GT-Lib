package org.gtreimagined.gtlib.integration.recipeviewer.emi.recipe;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.gui.SlotData;
import org.gtreimagined.gtlib.gui.SlotTypes;
import org.gtreimagined.gtlib.integration.recipeviewer.emi.GTEMIFluidIngredient;
import org.gtreimagined.gtlib.integration.recipeviewer.emi.GTFluidEmiStack;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.RecipeWidget;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;

import java.util.ArrayList;
import java.util.List;

public class RecipeMapRecipe extends ModularUIEmiRecipe {
    private final EmiRecipeCategory category;
    private final IRecipe recipe;

    public RecipeMapRecipe(EmiRecipeCategory category, IRecipe recipe, GuiProperties gui, Tier guiTier){
        super(id(recipe), () -> new RecipeWidget(recipe, GTAPI.get(RecipeMap.class, recipe.getMapLoc()), gui, guiTier));
        this.category = category;
        this.recipe = recipe;
        calculateSize();
    }

    private static ResourceLocation id(IRecipe recipe){
        return recipe.getTags().contains("emi_proxy") ? new ResourceLocation(recipe.getId().getNamespace(), "/" + recipe.getId().getPath()) : recipe.getId();
    }
    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        List<EmiIngredient> inputs = new ArrayList<>();
        this.recipe.getInputItems().forEach(i -> inputs.add(EmiIngredient.of(i)));
        this.recipe.getInputFluids().forEach(f -> inputs.add(new GTEMIFluidIngredient(f)));
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        List<EmiStack> outputs = new ArrayList<>();
        if (this.recipe.hasOutputItems()){
            for (ItemStack outputItem : this.recipe.getOutputItems(false)) {
                outputs.add(EmiStack.of(outputItem));
            }
        }
        if (recipe.hasOutputFluids()){
            for (FluidStack stack : recipe.getOutputFluids()){
                outputs.add(new GTFluidEmiStack(stack.getFluid(), stack.getTag(), stack.getAmount()));
            }
        }
        return outputs;
    }

}
