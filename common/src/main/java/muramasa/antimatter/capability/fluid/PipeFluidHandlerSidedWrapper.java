package muramasa.antimatter.capability.fluid;

import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.utils.FluidHooks;
import muramasa.antimatter.blockentity.pipe.BlockEntityFluidPipe;
import muramasa.antimatter.capability.FluidHandler;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class PipeFluidHandlerSidedWrapper extends FluidHandlerSidedWrapper{
    BlockEntityFluidPipe<?> pipe;
    FluidHandler<?> fluidHandler;
    public PipeFluidHandlerSidedWrapper(FluidHandler<?> fluidHandler, BlockEntityFluidPipe<?> fluidPipe, Direction side) {
        super(fluidHandler, fluidPipe.coverHandler.orElse(null), side);
        pipe = fluidPipe;
        this.fluidHandler = fluidHandler;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (side == null) return 0;
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
        int tank = fluidHandler.getInputTanks().getFirstAvailableTank(resource, false);
        if (tank == -1) return 0;
        int insert = fluidHandler.getInputTanks().getTank(tank).fill(resource, action);
        if (insert > 0 && action.execute()){
            pipe.setLastSide(side, tank);
        }
        return insert;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (side == null) return FluidStack.EMPTY;
        if (coverHandler != null && (coverHandler.get(side).blocksOutput(IFluidHandler.class, side) || coverHandler.onTransfer(resource, side, false, action.simulate()))) {
            return FluidStack.EMPTY;
        }
        return super.drain(resource, action);
    }
}
