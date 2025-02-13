package muramasa.antimatter.blockentity;

import earth.terrarium.botarium.common.fluid.base.PlatformFluidHandler;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import tesseract.TesseractCapUtils;

import java.util.Map;
import java.util.Optional;

public class BlockEntityCache {
    public static Optional<PlatformFluidHandler> getFluidHandlerCached(Level level, BlockPos pos, Direction side){
        return TesseractCapUtils.INSTANCE.getFluidHandler(level, pos, side);
    }
}
