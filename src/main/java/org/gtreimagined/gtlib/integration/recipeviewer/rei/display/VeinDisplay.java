package org.gtreimagined.gtlib.integration.recipeviewer.rei.display;

import brachy.modularui.integration.rei.recipe.ModularUIReiDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.GTLibREIClientPlugin;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.VeinWidget;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.WidgetUtils;
import org.gtreimagined.gtlib.worldgen.vein.Vein;

import java.util.List;

public class VeinDisplay extends ModularUIReiDisplay {
    private final List<EntryIngredient> input, output;
    public VeinDisplay(Vein vein){
        super(VeinWidget.id(vein), () -> new VeinWidget(vein), GTLibREIClientPlugin.VEIN_ID);
        input = WidgetUtils.getDimensionSlotItems(vein.dimensions()).stream().map(EntryIngredients::of).toList();
        output = VeinWidget.getStacks(vein).stream().map(l -> l.stream().map(EntryStacks::of).toList()).map(EntryIngredient::of).toList();
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
