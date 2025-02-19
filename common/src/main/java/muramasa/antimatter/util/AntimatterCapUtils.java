package muramasa.antimatter.util;

import earth.terrarium.botarium.common.fluid.base.PlatformFluidHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Optional;

public interface AntimatterCapUtils {
    AntimatterCapUtils INSTANCE = ImplLoader.load(AntimatterCapUtils.class);

    Optional<PlatformFluidHandler> getFluidHandler(Level level, BlockPos pos, Direction side);
}
