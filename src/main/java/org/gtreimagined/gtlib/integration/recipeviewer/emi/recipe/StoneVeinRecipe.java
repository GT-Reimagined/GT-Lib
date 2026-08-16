package org.gtreimagined.gtlib.integration.recipeviewer.emi.recipe;

import brachy.modularui.integration.emi.recipe.ModularUIEmiCategory;
import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.integration.recipeviewer.StoneVein;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.SmallOreWidget;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.StoneVeinWidget;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.WidgetUtils;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;

import java.util.List;

public class StoneVeinRecipe extends ModularUIEmiRecipe {
    final List<EmiIngredient> inputs;
    final List<EmiStack> outputs;
    public static final EmiRecipeCategory CATEGORY = new ModularUIEmiCategory(new ResourceLocation(Ref.ID, "stone_veins"), EmiStack.of(Items.IRON_ORE));

    public StoneVeinRecipe(StoneVein stoneVein) {
        super(StoneVeinWidget.id(stoneVein), () -> new StoneVeinWidget(stoneVein));
        inputs = WidgetUtils.getDimensionSlotItems(stoneVein.stoneLayer().dimensions()).stream().map(EmiStack::of).map(e -> (EmiIngredient)e).toList();
        outputs = StoneVeinWidget.getOutputs(stoneVein).stream().map(EmiStack::of).toList();
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
