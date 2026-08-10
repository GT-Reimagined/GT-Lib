package org.gtreimagined.gtlib.integration.recipeviewer.rei.category;

import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.integration.rei.recipe.ModularUIReiCategory;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import mezz.jei.api.constants.VanillaTypes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.GTLibJEIPlugin;
import org.gtreimagined.gtlib.mui.BarDir;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.gui.SlotData;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.integration.recipeviewer.renderer.IRecipeInfoRenderer;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.recipe.map.SubCategory;
import org.gtreimagined.gtlib.util.RegistryUtils;
import org.gtreimagined.gtlib.util.int4;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecipeMapCategory extends ModularUIReiCategory<RecipeMapDisplay> {

    protected final CategoryIdentifier<RecipeMapDisplay> loc;
    protected Renderer icon;

    public RecipeMapCategory(IRecipeMap map, ResourceLocation iconId) {
        loc = CategoryIdentifier.of(map.getLoc());
        initIcon(map.getIcon(), iconId);
    }

    public RecipeMapCategory(ResourceLocation subCategoryID, SubCategory subCategory) {
        loc = CategoryIdentifier.of(subCategoryID);
        initIcon(subCategory.icon().get(), null);
    }

    private void initIcon(Object icon, ResourceLocation iconId){
        if (icon != null) {
            if (icon instanceof ItemStack itemStack) {
                this.icon = EntryStacks.of(itemStack);
            }
            if (icon instanceof ItemLike item) {
                this.icon = EntryStacks.of(item);
            }
            if (icon instanceof ResourceLocation resourceLocation){
                this.icon = Widgets.createTexturedWidget(resourceLocation, 0, 0, 0, 0, 16, 16, 16, 16);
            }
        } else {
            Item item = iconId == null ? Data.DEBUG_SCANNER : RegistryUtils.getItemFromID(iconId);
            if (item == Items.AIR) item = Data.DEBUG_SCANNER;
            this.icon = EntryStacks.of(item);
        }
    }

    @Override
    public int getMaxDisplayHeight() {
        return 170;
    }

    @Override
    public Renderer getIcon() {
        return icon;
    }

    @Override
    public CategoryIdentifier<? extends RecipeMapDisplay> getCategoryIdentifier() {
        return loc;
    }
}
