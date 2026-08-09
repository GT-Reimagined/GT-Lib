package org.gtreimagined.gtlib.integration.recipeviewer.jei.category;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.integration.recipeviewer.StoneVein;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.GTLibJEIPlugin;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.StoneVeinWidget;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.WidgetUtils;
import org.gtreimagined.gtlib.material.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.ORE;

public class StoneVeinCategory extends ModularUIJeiCategory<StoneVein> {
    IDrawable icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.IRON_ORE.getDefaultInstance());
    public static final RecipeType<StoneVein> STONE_VEINS = new RecipeType<>(new ResourceLocation(Ref.ID, "stone_veins"), StoneVein.class);
    public StoneVeinCategory() {
        super(StoneVeinWidget::new, r -> StoneVeinWidget.id(r, false));
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public RecipeType<StoneVein> getRecipeType() {
       return STONE_VEINS;
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
    public void setupRecipeIngredients(IRecipeLayoutBuilder builder, StoneVein recipe, IFocusGroup focuses) {

        Material material = recipe.ore().material();//i == 0 ? recipe.primary() : i == 1 ? recipe.secondary() : i == 2 ? recipe.between() : recipe.sporadic();
        builder.addOutputSlot()
                .addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ORE.get().get(material, VanillaStoneTypes.STONE).asItem()));
        builder.addOutputSlot()
                .addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(recipe.stoneLayer().block()));
        for (ItemStack stack : WidgetUtils.getDimensionSlotItems(recipe.stoneLayer().dimensions())){
            builder.addInputSlot().addItemStack(stack);
        }
    }
}
