package org.gtreimagined.gtlib.integration.recipeviewer.rei.display;

import brachy.modularui.integration.rei.recipe.ModularUIReiDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import org.gtreimagined.gtlib.integration.recipeviewer.StoneVein;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.GTLibREIClientPlugin;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.StoneVeinWidget;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.WidgetUtils;

import java.util.List;

public class StoneVeinDisplay extends ModularUIReiDisplay {
    private final List<EntryIngredient> input, output;
    public StoneVeinDisplay(StoneVein vein) {
        super(StoneVeinWidget.id(vein), () -> new StoneVeinWidget(vein), GTLibREIClientPlugin.STONE_VEIN_ID);
        input = WidgetUtils.getDimensionSlotItems(vein.stoneLayer().dimensions()).stream().map(EntryIngredients::of).toList();
        output = StoneVeinWidget.getOutputs(vein).stream().map(EntryIngredients::of).toList();
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
