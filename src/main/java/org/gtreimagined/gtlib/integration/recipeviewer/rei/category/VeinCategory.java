package org.gtreimagined.gtlib.integration.recipeviewer.rei.category;

import brachy.modularui.integration.rei.recipe.ModularUIReiCategory;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.display.SmallOreDisplay;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.display.VeinDisplay;

public class VeinCategory extends ModularUIReiCategory<VeinDisplay> {
    static final EntryStack<ItemStack> ICON = EntryStacks.of(Items.IRON_ORE);
    public static final CategoryIdentifier<VeinDisplay> ID = CategoryIdentifier.of(Ref.ID, "veins");
    @Override
    public int getMaxDisplayHeight() {
        return 120;
    }

    @Override
    public CategoryIdentifier<? extends VeinDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public Renderer getIcon() {
        return ICON;
    }
}
