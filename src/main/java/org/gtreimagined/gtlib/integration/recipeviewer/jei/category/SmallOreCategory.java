package org.gtreimagined.gtlib.integration.recipeviewer.jei.category;

import brachy.modularui.integration.jei.recipe.ModularUIJeiCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.GTLibJEIPlugin;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.SmallOreWidget;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.WidgetUtils;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;

public class SmallOreCategory extends ModularUIJeiCategory<SmallOre> {
    IDrawable icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.IRON_ORE.getDefaultInstance());
    public static final RecipeType<SmallOre> SMALL_ORES = new RecipeType<>(new ResourceLocation(Ref.ID, "small_ores"), SmallOre.class);
    public SmallOreCategory() {
        super(SmallOreWidget::new, SmallOre::id);
    }

    @Override
    public Component getTitle() {
        return Utils.translatable("jei.category.gtlib.small_ores");
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
    public void setupRecipeIngredients(IRecipeLayoutBuilder builder, SmallOre recipe, IFocusGroup focuses) {
        for (var list : SmallOreWidget.getOutputs(recipe)){
            builder.addOutputSlot().addItemStacks(list);
        }
        for (ItemStack stack : WidgetUtils.getDimensionSlotItems(recipe.dimensions())){
            builder.addInputSlot().addItemStack(stack);
        }
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public RecipeType<SmallOre> getRecipeType() {
       return SMALL_ORES;
    }

}
