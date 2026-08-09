package org.gtreimagined.gtlib.integration.recipeviewer.emi.recipe;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.SmallOreWidget;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.VeinWidget;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.WidgetUtils;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;
import org.gtreimagined.gtlib.worldgen.vein.Vein;

import java.util.List;

public class VeinRecipe extends ModularUIEmiRecipe {
    final List<EmiIngredient> inputs;
    final List<EmiStack> outputs;
    static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(new ResourceLocation(Ref.ID, "veins"), EmiStack.of(Items.IRON_ORE));

    public VeinRecipe(Vein vein) {
        super(new ResourceLocation(vein.getDomain(), "/" + vein.getId()), () -> new VeinWidget(vein));
        inputs = WidgetUtils.getDimensionSlotItems(vein.dimensions()).stream().map(EmiStack::of).map(e -> (EmiIngredient)e).toList();
        outputs = VeinWidget.getStacks(vein).stream().flatMap(l -> l.stream().map(EmiStack::of)).toList();
        calculateSize();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }
}
