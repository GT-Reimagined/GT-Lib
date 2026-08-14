package org.gtreimagined.gtlib.integration.recipeviewer.rei.display;

import brachy.modularui.integration.rei.recipe.ModularUIReiDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.GTLibREIClientPlugin;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.SmallOreWidget;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.WidgetUtils;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;

import java.util.List;

public class SmallOreDisplay extends ModularUIReiDisplay {
    private final List<EntryIngredient> input, output;
    public SmallOreDisplay(SmallOre smallOre) {
        super(SmallOreWidget.id(smallOre), () -> new SmallOreWidget(smallOre), GTLibREIClientPlugin.SMALL_ORE_ID);
        input = WidgetUtils.getDimensionSlotItems(smallOre.dimensions()).stream().map(EntryIngredients::of).toList();
        output = SmallOreWidget.getOutputs(smallOre).stream().map(l -> l.stream().map(EntryStacks::of).toList()).map(EntryIngredient::of).toList();
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
