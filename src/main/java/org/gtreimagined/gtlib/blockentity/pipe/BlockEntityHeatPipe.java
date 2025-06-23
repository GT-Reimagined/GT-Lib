package org.gtreimagined.gtlib.blockentity.pipe;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.gtreimagined.gtlib.pipe.types.HeatPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import tesseract.api.heat.HeatFactoryGrid;
import tesseract.api.heat.HeatFactoryNetwork;
import tesseract.api.heat.IHeatHandler;
import tesseract.api.heat.IHeatPipe;

import java.util.Collection;

public class BlockEntityHeatPipe<T extends HeatPipe<T>> extends BlockEntityPipe<T> implements IHeatPipe {
    HeatFactoryNetwork network;
    public BlockEntityHeatPipe(T type, BlockPos pos, BlockState state) {
        super(type, pos, state);

    }

    @Override
    public Class<?> getCapClass() {
        return IHeatHandler.class;
    }

    @Override
    protected void register() {
        HeatFactoryGrid.INSTANCE.addElement(this);
    }

    @Override
    protected boolean deregister() {
        HeatFactoryGrid.INSTANCE.removeElement(this);
        return true;
    }

    @Override
    public int temperatureCoefficient() {
        return this.type.conductivity;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public void getNeighbours(Collection<IHeatPipe> neighbours) {
        for (Direction dir : Direction.values()) {
            BlockEntityPipe<?> pipe = getPipe(dir);
            if (pipe instanceof BlockEntityHeatPipe<?> heatPipe) {
                if (heatPipe.connects(dir.getOpposite()) && connects(dir)){
                    neighbours.add(heatPipe);
                }
            }
        }
    }

    @Override
    public HeatFactoryNetwork getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(HeatFactoryNetwork network) {
        this.network = network;
    }
}
