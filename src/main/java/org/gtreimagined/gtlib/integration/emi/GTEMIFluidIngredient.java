package org.gtreimagined.gtlib.integration.emi;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;
import org.gtreimagined.gtlib.util.FluidUtils;
import org.gtreimagined.gtlib.util.Utils;

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
        return 1;
    }

    @Override
    public EmiIngredient setChance(float v) {
        return this;
    }

    @Override
    public void render(GuiGraphics graphics, int i, int i1, float v, int i2) {

    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        List<ClientTooltipComponent> list = new ArrayList<>();

        FluidStack stack = getCurrentStack();
        if (stack.isEmpty()) {
            list.add(ClientTooltipComponent.create(Utils.literal("Invalid FLuid Ingredient").withStyle(ChatFormatting.RED).getVisualOrderText()));
            return list;
        }
        int mb = stack.getAmount();
        list.add(ClientTooltipComponent.create(stack.getFluid().getFluidType().getDescription(stack).getVisualOrderText()));
        list.add(ClientTooltipComponent.create(Utils.translatable("gtlib.tooltip.fluid.amount", mb + " L").withStyle(ChatFormatting.BLUE).getVisualOrderText()));
        list.add(ClientTooltipComponent.create(Utils.translatable("gtlib.tooltip.fluid.temp", FluidUtils.getFluidTemperature(stack.getFluid())).withStyle(ChatFormatting.RED).getVisualOrderText()));
        String liquid = !FluidUtils.isFluidGaseous(stack.getFluid()) ? "liquid" : "gas";
        list.add(ClientTooltipComponent.create(Utils.translatable("gtlib.tooltip.fluid." + liquid).withStyle(ChatFormatting.GREEN).getVisualOrderText()));

        return list;
    }

    FluidStack getCurrentStack(){
        if (fluids.isEmpty()) return FluidStack.EMPTY;
        if (fluids.get(0).getKey() instanceof Fluid fluid){
            return new FluidStack(fluid, (int) amount);
        }
        return FluidStack.EMPTY;
    }
}
