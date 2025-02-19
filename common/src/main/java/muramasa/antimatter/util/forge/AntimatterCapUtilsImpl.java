package muramasa.antimatter.util.forge;

import earth.terrarium.botarium.common.fluid.base.PlatformFluidHandler;
import earth.terrarium.botarium.common.fluid.utils.FluidHooks;
import earth.terrarium.botarium.forge.fluid.ForgeFluidHandler;
import muramasa.antimatter.capability.fluid.forge.CauldronWrapper;
import muramasa.antimatter.util.AntimatterCapUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class AntimatterCapUtilsImpl implements AntimatterCapUtils {
    @Override
    public Optional<PlatformFluidHandler> getFluidHandler(Level level, BlockPos pos, Direction side){
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity == null){
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof AbstractCauldronBlock){
                return Optional.of(new ForgeFluidHandler(new CauldronWrapper(state, level, pos)));
            }
            return Optional.empty();
        }
        return FluidHooks.safeGetBlockFluidManager(entity, side);
    }
}
