package org.gtreimagined.gtlib.blockentity.pipe;

import org.gtreimagined.gtlib.pipe.types.HeatPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import tesseract.TesseractGraphWrappers;
import tesseract.api.heat.IHeatHandler;
import tesseract.api.heat.IHeatPipe;

public class BlockEntityHeatPipe<T extends HeatPipe<T>> extends BlockEntityPipe<T> implements IHeatPipe {

    public BlockEntityHeatPipe(T type, BlockPos pos, BlockState state) {
        super(type, pos, state);

    }

    @Override
    public Class<?> getCapClass() {
        return IHeatHandler.class;
    }

    @Override
    protected void register() {
        TesseractGraphWrappers.HEAT_CONTROLLER.registerConnector(getLevel(), getBlockPos().asLong(), this, isConnector());
    }

    @Override
    protected boolean deregister() {
        return TesseractGraphWrappers.HEAT_CONTROLLER.remove(level, getBlockPos().asLong());
    }

    @Override
    public int temperatureCoefficient() {
        return this.type.conductivity;
    }
}
