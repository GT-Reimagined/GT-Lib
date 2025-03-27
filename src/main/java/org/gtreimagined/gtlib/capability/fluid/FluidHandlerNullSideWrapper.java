package org.gtreimagined.gtlib.capability.fluid;

import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FluidHandlerNullSideWrapper implements IFluidNode {
    IFluidNode fluidHandler;

    public FluidHandlerNullSideWrapper(IFluidNode fluidHandler) {
        this.fluidHandler = fluidHandler;
    }

    @Override
    public int getPriority(Direction direction) {
        return fluidHandler.getPriority(direction);
    }

    @Override
    public boolean canInput() {
        return false;
    }

    @Override
    public boolean canOutput() {
        return false;
    }

    @Override
    public boolean canInput(Direction direction) {
        return fluidHandler.canInput(direction);
    }

    @Override
    public boolean canOutput(Direction direction) {
        return fluidHandler.canOutput(direction);
    }

    @Override
    public boolean canInput(FluidStack fluid, Direction direction) {
        return fluidHandler.canInput(fluid, direction);
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(int amount, FluidAction fluidAction) {
       return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
       return FluidStack.EMPTY;
    }

    @Override
    public int getTanks() {
        return fluidHandler.getTanks();
    }

    @Override
    public int getTankCapacity(int tankSlot) {
        return fluidHandler.getTankCapacity(tankSlot);
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        return fluidHandler.getFluidInTank(i);
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        return fluidHandler.isFluidValid(i, fluidStack);
    }
}
