package org.gtreimagined.gtlib.integration.recipeviewer.rei.category;

import brachy.modularui.integration.rei.recipe.ModularUIReiDisplay;
import com.google.common.collect.ImmutableList;
import dev.architectury.fluid.FluidStack;
import lombok.Getter;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.REIUtils;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.RecipeWidget;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import org.gtreimagined.gtlib.util.FluidUtils;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.gtreimagined.gtlib.integration.recipeviewer.GTLibRecipeViewerPlugin.intToSuperScript;

public class RecipeMapDisplay extends ModularUIReiDisplay {
    private final List<EntryIngredient> input, output;
    @Getter
    private final IRecipe recipe;

    public RecipeMapDisplay(IRecipe recipe, IRecipeMap map, GuiProperties gui, Tier tier, ResourceLocation categoryId){
        super(recipe.getId(), () -> new RecipeWidget(recipe, map, gui, map.getGuiTier() == null ? tier : map.getGuiTier()), CategoryIdentifier.of(categoryId));
        this.recipe = recipe;
        this.input = new ArrayList<>(recipe.getInputItems().size() + recipe.getInputFluids().size());
        this.input.addAll(recipe.getInputItems().stream().map(EntryIngredients::ofIngredient).toList());
        this.input.addAll(recipe.getInputFluids().stream().map(i -> {
            return EntryIngredient.of(Arrays.stream(i.getStacks()).map(REIUtils::toREIFLuidStack).map(EntryStacks::of).toList());
        }).toList());
        this.output = new ArrayList<>(recipe.getOutputItems(false).size() + recipe.getOutputFluids().size());
        this.output.addAll(recipe.getOutputItems().stream().map(EntryIngredients::of).toList());
        this.output.addAll(recipe.getOutputFluids().stream().map(REIUtils::toREIFLuidStack).map(EntryIngredients::of).toList());
    }

    public RecipeMapDisplay(IRecipe recipe, IRecipeMap map, GuiProperties gui, Tier tier){
        this(recipe, map, gui, tier, map.getLoc());
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return input;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return output;
    }

}
