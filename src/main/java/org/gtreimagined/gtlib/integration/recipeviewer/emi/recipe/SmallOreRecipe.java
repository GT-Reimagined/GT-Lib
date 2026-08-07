package org.gtreimagined.gtlib.integration.recipeviewer.emi.recipe;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.SmallOreWidget;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.WidgetUtils;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;

import java.util.List;
import java.util.function.Supplier;

public class SmallOreRecipe extends ModularUIEmiRecipe {
    final List<EmiIngredient> inputs;
    final List<EmiStack> outputs;
    static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(new ResourceLocation(Ref.ID, "small_ores"), EmiStack.of(Items.IRON_ORE));

    public SmallOreRecipe(SmallOre smallOre) {
        super(new ResourceLocation(smallOre.getDomain(), "/" + smallOre.getId()), () -> new SmallOreWidget(smallOre));
        inputs = WidgetUtils.getDimensionSlotItems(smallOre.dimensions()).stream().map(EmiStack::of).map(e -> (EmiIngredient)e).toList();
        outputs = SmallOreWidget.getOutputs(smallOre).stream().flatMap(l -> l.stream().map(EmiStack::of)).toList();
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
