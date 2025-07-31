package org.gtreimagined.gtlib.capability.pipe;

import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityFluidPipe;
import org.gtreimagined.gtlib.capability.FluidHandler;

public class PipeFluidHandler extends FluidHandler<BlockEntityFluidPipe<?>> {
    public PipeFluidHandler(BlockEntityFluidPipe<?> tile, int capacity, int inputCount, int outputCount) {
        super(tile, capacity, inputCount, outputCount);
    }
}
