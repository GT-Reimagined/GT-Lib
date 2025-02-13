package muramasa.antimatter.util;

import earth.terrarium.botarium.common.fluid.base.PlatformFluidHandler;
import muramasa.antimatter.capability.item.PlatformItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public interface AntimatterCapUtils {
    AntimatterCapUtils INSTANCE = ImplLoader.load(AntimatterCapUtils.class);
    Optional<PlatformItemHandler> getItemHandler(BlockEntity entity, Direction side);


    Optional<PlatformFluidHandler> getFluidHandler(Level level, BlockPos pos, Direction side);
}
