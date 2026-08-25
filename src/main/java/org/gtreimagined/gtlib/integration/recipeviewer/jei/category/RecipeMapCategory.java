package org.gtreimagined.gtlib.integration.recipeviewer.jei.category;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.gui.SlotTypes;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.GTLibJEIPlugin;
import org.gtreimagined.gtlib.mui.BarDir;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.gui.SlotData;
import org.gtreimagined.gtlib.integration.recipeviewer.renderer.IRecipeInfoRenderer;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.GTLibJEIPlugin;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.RecipeWidget;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.recipe.map.SubCategory;
import org.gtreimagined.gtlib.util.RegistryUtils;
import org.gtreimagined.gtlib.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipeMapCategory extends ModularUIJeiCategory<IRecipe> {
    protected static int JEI_OFFSET_X = 1;
    protected static int JEI_OFFSET_Y = 1;
    protected final RecipeType<IRecipe> type;
    protected Component title;
    protected IDrawable icon;
    private RecipeMapCategory(IRecipeMap map, RecipeType<IRecipe> type, GuiProperties gui, Tier defaultTier){
        super(r -> new RecipeWidget(r, map, gui, map.getGuiTier() == null ? defaultTier : map.getGuiTier()), Recipe::getId);
        this.type = type;
    }

    public RecipeMapCategory(IRecipeMap map, RecipeType<IRecipe> type, GuiProperties gui, Tier defaultTier, ResourceLocation iconId){
        this(map, type, gui, defaultTier);
        initIcon(map.getIcon(), iconId);
    }

    public RecipeMapCategory(IRecipeMap map, RecipeType<IRecipe> type, GuiProperties gui, Tier defaultTier, ResourceLocation subCategoryId, SubCategory subCategory){
        this(map, type, gui, defaultTier);
        initIcon(subCategory.icon().get(), null);
    }

    private void initIcon(Object icon, ResourceLocation iconId){
        if (icon != null) {
            if (icon instanceof ItemStack itemStack) {
                this.icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, itemStack);
            }
            if (icon instanceof ItemLike item) {
                this.icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(item));
            }
            if (icon instanceof ResourceLocation resourceLocation){
                this.icon = GTLibJEIPlugin.guiHelper.drawableBuilder(resourceLocation, 0, 0, 16, 16).setTextureSize(16, 16).build();
            }
        } else {
            Item item = iconId == null ? Data.DEBUG_SCANNER : RegistryUtils.getItemFromID(iconId);
            if (item == Items.AIR) item = Data.DEBUG_SCANNER;
            this.icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(item, 1));
        }
    }


    @Override
    public RecipeType<IRecipe> getRecipeType() {
        return type;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getMaxHeight() {
        return 170;
    }

    @Override
    public int getMaxWidth() {
        return 170;
    }

    @Override
    public void setupRecipeIngredients(IRecipeLayoutBuilder builder, IRecipe recipe, IFocusGroup focuses) {
        if (recipe.hasInputItems()) recipe.getInputItems().forEach(i -> builder.addInputSlot().addIngredients(i));
        if (recipe.hasOutputItems()) recipe.getOutputItems(false).forEach(i -> builder.addOutputSlot().addItemStack(i));
        if (recipe.hasInputFluids()) recipe.getInputFluids().forEach(f -> builder.addInputSlot().addIngredients(ForgeTypes.FLUID_STACK, List.of(f.getStacks())));
        if (recipe.hasOutputFluids()) recipe.getOutputFluids().forEach(f -> builder.addOutputSlot().addIngredient(ForgeTypes.FLUID_STACK, f));
    }
}
