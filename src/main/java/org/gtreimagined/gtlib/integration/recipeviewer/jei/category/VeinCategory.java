package org.gtreimagined.gtlib.integration.recipeviewer.jei.category;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.GTLibJEIPlugin;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.VeinWidget;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.WidgetUtils;
import org.gtreimagined.gtlib.worldgen.vein.Vein;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class VeinCategory extends ModularUIJeiCategory<Vein> {
    IDrawable icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.IRON_ORE.getDefaultInstance());
    public static final RecipeType<Vein> VEINS = new RecipeType<>(new ResourceLocation(Ref.ID, "veins"), Vein.class);
    public VeinCategory() {
        super(VeinWidget::new, VeinWidget::id);
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public RecipeType<Vein> getRecipeType() {
       return VEINS;
    }

    @Override
    public int getMaxWidth() {
        return 170;
    }

    @Override
    public int getMaxHeight() {
        return 120;
    }

    @Override
    public void setupRecipeIngredients(IRecipeLayoutBuilder builder, Vein recipe, IFocusGroup focuses) {
        VeinWidget.getStacks(recipe).forEach(l -> builder.addOutputSlot().addItemStacks(l));
        for (ItemStack stack : WidgetUtils.getDimensionSlotItems(recipe.dimensions())){
            builder.addInputSlot().addItemStack(stack);
        }
    }
}
