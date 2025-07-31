package org.gtreimagined.gtlib.pipe.types;

import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityHeatPipe;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.pipe.BlockHeatPipe;
import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.stream.Collectors;

public class HeatPipe<T extends HeatPipe<T>> extends PipeType<T> {

    public final int conductivity;

    public HeatPipe(String domain, Material material, int conductivity) {
        super(domain, material, BlockEntityHeatPipe::new);
        this.conductivity = conductivity;
    }

    @Override
    public Set<Block> getBlocks() {
        return sizes.stream().map(s -> new BlockHeatPipe(this, s)).collect(Collectors.toSet());
    }

    @Override
    public String getType() {
        return "heat_pipe";
    }

    @Override
    public String getTypeName() {
        return "heat_pipe";
    }
}
