package org.gtreimagined.gtlib.mui.widgets;

import brachy.modularui.screen.RichTooltip;
import brachy.modularui.widgets.slot.FluidSlot;
import lombok.experimental.Accessors;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import org.gtreimagined.gtlib.integration.xei.GTLibXEIPlugin;
import org.gtreimagined.gtlib.util.FluidUtils;
import org.gtreimagined.gtlib.util.Utils;

import java.util.ArrayList;
import java.util.List;

@Accessors(fluent = true, chain = true)
public class GTFluidSlot extends FluidSlot {
    @Override
    protected void addTooltip(RichTooltip tooltip) {
        FluidStack fluid = this.getFluidStack();
        if (fluid == null || fluid.isEmpty()) return;
        tooltip.addLine(fluid.getDisplayName());
        List<Component> str = new ArrayList<>();
        int mb = fluid.getAmount();
        str.add(Utils.translatable("gtlib.tooltip.fluid.amount", mb + " L").withStyle(ChatFormatting.BLUE));
        str.add(Utils.translatable("gtlib.tooltip.fluid.temp", FluidUtils.getFluidTemperature(fluid.getFluid())).withStyle(ChatFormatting.RED));
        String liquid = !FluidUtils.isFluidGaseous(fluid.getFluid()) ? "liquid" : "gas";
        str.add(Utils.translatable("gtlib.tooltip.fluid." + liquid).withStyle(ChatFormatting.GREEN));
        GTLibXEIPlugin.addModDescriptor(str, fluid);
        for (Component c : str) {
            tooltip.addLine(c);
        }
    }
}
