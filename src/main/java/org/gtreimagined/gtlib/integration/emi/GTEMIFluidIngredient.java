package org.gtreimagined.gtlib.integration.emi;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraftforge.fluids.FluidStack;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;

import java.util.ArrayList;
import java.util.List;

public class GTEMIFluidIngredient implements EmiIngredient {
    final List<EmiStack> fluids;
    final long amount;

    public GTEMIFluidIngredient(FluidIngredient ingredient) {
        this.fluids = new ArrayList<>();
        for (FluidStack stack : ingredient.getStacks()){
           this.fluids.add(ForgeEmiStack.of(stack));
        }
        amount = ingredient.getAmount();
    }

    private GTEMIFluidIngredient(List<EmiStack> fluids, long amount){
        this.fluids = fluids;
        this.amount = amount;
    }

    @Override
    public List<EmiStack> getEmiStacks() {
        return fluids;
    }

    @Override
    public EmiIngredient copy() {
        List<EmiStack> stacks = new ArrayList<>(fluids);
        return new GTEMIFluidIngredient(stacks, amount);
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public EmiIngredient setAmount(long l) {
        return new GTEMIFluidIngredient(new ArrayList<>(fluids), l);
    }

    @Override
    public float getChance() {
        return 0;
    }

    @Override
    public EmiIngredient setChance(float v) {
        return null;
    }

    @Override
    public void render(PoseStack poseStack, int i, int i1, float v, int i2) {

    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        return List.of();
    }
}
