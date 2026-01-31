package org.gtreimagined.gtlib.pipe;

import org.gtreimagined.gtlib.pipe.types.HeatPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockHeatPipe<T extends HeatPipe<T>> extends BlockPipe<T> {

    public BlockHeatPipe(T type, PipeSize size) {
        super(type, size, 0);
    }

    @Override
    //@ParametersAreNotNullByDefault
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
        super.entityInside(state, worldIn, pos, entityIn);
        /*if (!(entityIn instanceof LivingEntity)) return;
        BlockEntityHeatPipe<?> pipe = (BlockEntityHeatPipe) worldIn.getBlockEntity(pos);
        int temp = pipe.getTemperature();
        if (temp > 50) {
            entityIn.hurt(DamageSource.GENERIC, Mth.clamp((temp-10)/2, 2, 20));
        }*/
    }
}
