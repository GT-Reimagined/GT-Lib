package muramasa.antimatter.capability.pipe;

import muramasa.antimatter.blockentity.pipe.BlockEntityFluidPipe;
import muramasa.antimatter.capability.FluidHandler;

public class PipeFluidHandler extends FluidHandler<BlockEntityFluidPipe<?>> {
    public PipeFluidHandler(BlockEntityFluidPipe<?> tile, int capacity, int inputCount, int outputCount) {
        super(tile, capacity, inputCount, outputCount);
    }
}
