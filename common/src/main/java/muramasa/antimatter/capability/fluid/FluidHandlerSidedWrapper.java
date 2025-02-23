package muramasa.antimatter.capability.fluid;

import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.base.FluidSnapshot;
import earth.terrarium.botarium.common.fluid.utils.FluidHooks;
import muramasa.antimatter.capability.CoverHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FluidHandlerSidedWrapper implements IFluidNode {
    protected IFluidNode fluidHandler;
    protected Direction side;
    CoverHandler<?> coverHandler;

    public FluidHandlerSidedWrapper(IFluidNode fluidHandler, CoverHandler<?> coverHandler, Direction side) {
        this.fluidHandler = fluidHandler;
        this.coverHandler = coverHandler;
        this.side = side;
    }

    @Override
    public int getTanks() {
        return fluidHandler.getTanks();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return fluidHandler.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return fluidHandler.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return fluidHandler.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (coverHandler != null) {
            if (coverHandler.get(side).blocksInput(IFluidHandler.class, side)) {
                return 0;
            }
            int oldAmount = resource.getAmount();
            if(coverHandler.onTransfer(resource, side, true, action.simulate())) return oldAmount - resource.getAmount();
        }

        if (!fluidHandler.canInput(resource, side) || !fluidHandler.canInput(side)) {
            return 0;
        }
        return fluidHandler.fill(resource, action);
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (coverHandler != null && (coverHandler.get(side).blocksOutput(IFluidHandler.class, side) || coverHandler.onTransfer(resource, side, false, action.simulate()))) {
            return FluidStack.EMPTY;
        }
        if (!fluidHandler.canOutput(side)) return FluidStack.EMPTY;
        return fluidHandler.drain(resource, action);
    }

    @Override
    public int getPriority(Direction direction) {
        return fluidHandler.getPriority(direction);
    }

    @Override
    public boolean canOutput() {
        return fluidHandler.canOutput();
    }

    @Override
    public boolean canInput() {
        return fluidHandler.canInput();
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
}
