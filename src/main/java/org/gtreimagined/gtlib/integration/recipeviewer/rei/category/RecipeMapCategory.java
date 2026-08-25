package org.gtreimagined.gtlib.integration.recipeviewer.rei.category;

import brachy.modularui.integration.rei.recipe.ModularUIReiCategory;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.Item;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.display.RecipeMapDisplay;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.recipe.map.SubCategory;
import org.gtreimagined.gtlib.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

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
                this.icon = (guiGraphics, rectangle, mouseX, mouseY, delta) -> {
                    guiGraphics.blit(resourceLocation, rectangle.x, rectangle.y, 0, 0, 0, 16, 16, 16, 16);
                };
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
