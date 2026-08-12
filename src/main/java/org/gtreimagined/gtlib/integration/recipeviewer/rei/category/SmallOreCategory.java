package org.gtreimagined.gtlib.integration.recipeviewer.rei.category;

import brachy.modularui.integration.rei.recipe.ModularUIReiCategory;
import brachy.modularui.integration.rei.recipe.ModularUIReiDisplay;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.display.SmallOreDisplay;

public class SmallOreCategory extends ModularUIReiCategory<SmallOreDisplay> {
    static final EntryStack<ItemStack> ICON = EntryStacks.of(Items.IRON_ORE);
    public static final CategoryIdentifier<SmallOreDisplay> ID = CategoryIdentifier.of(Ref.ID, "small_ores");
    @Override
    public int getMaxDisplayHeight() {
        return 120;
    }

    @Override
    public CategoryIdentifier<? extends SmallOreDisplay> getCategoryIdentifier() {
        return ID;
    }

    @Override
    public Renderer getIcon() {
        return ICON;
    }
}
