package org.gtreimagined.gtlib.integration.emi;

import dev.emi.emi.api.stack.FluidEmiStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.gtreimagined.gtlib.util.FluidUtils;
import org.gtreimagined.gtlib.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class GTFluidEmiStack extends FluidEmiStack {
    public GTFluidEmiStack(Fluid fluid, @Nullable CompoundTag nbt, long amount) {
        super(fluid, nbt, amount);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        List<ClientTooltipComponent> tooltip = super.getTooltip();
        tooltip.set(1, ClientTooltipComponent.create(Utils.translatable("gtlib.tooltip.fluid.amount", amount + " L").withStyle(ChatFormatting.BLUE).getVisualOrderText()));
        Fluid fluid = (Fluid) getKey();
        tooltip.add(2, ClientTooltipComponent.create(Utils.translatable("gtlib.tooltip.fluid.temp", FluidUtils.getFluidTemperature(fluid)).withStyle(ChatFormatting.RED).getVisualOrderText()));
        String liquid = !FluidUtils.isFluidGaseous(fluid) ? "liquid" : "gas";
        tooltip.add(3, ClientTooltipComponent.create(Utils.translatable("gtlib.tooltip.fluid." + liquid).withStyle(ChatFormatting.GREEN).getVisualOrderText()));
        return tooltip;
    }
}
