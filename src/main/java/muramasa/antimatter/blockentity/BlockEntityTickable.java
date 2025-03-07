package muramasa.antimatter.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityTickable<T extends BlockEntityTickable<T>> extends BlockEntityBase<T> {

    private boolean hadFirstTick;

    public BlockEntityTickable(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected void serverTick(Level level, BlockPos pos, BlockState state) {

    }
    protected void clientTick(Level level, BlockPos pos, BlockState state) {

    }

    protected void tick(Level level, BlockPos pos, BlockState state) {
        level.getProfiler().push("GTLibTileTick");
        if (!hadFirstTick) {
            if (level.isClientSide()) onFirstTickClient(level, pos, state);
            else onFirstTickServer(level, pos, state);
            hadFirstTick = true;
        }
        if (level.isClientSide()) {
            if (canClientTick()) { //TODO figure out if this change can cause any amount of client lag
                clientTick(level, pos, state);
            }
        } else {
            serverTick(level, pos, state);
        }
        level.getProfiler().pop();
    }

    protected boolean canClientTick(){
        return false;
    }

    public static <T extends BlockEntity> void commonTick(Level level, BlockPos pos, BlockState state, T tile) {
        if (tile instanceof BlockEntityTickable<?> tick) {
            tick.tick(level, pos, state);
        }
    }

    public boolean hadFirstTick() {
        return hadFirstTick;
    }


    public void onFirstTick() {

    }

    /**
     * Override these to do any initialization that requires the World and/or BlockState reference.
     */
    public void onFirstTickServer(Level level, BlockPos pos, BlockState state) {

    }

    public void onFirstTickClient(Level level, BlockPos pos, BlockState state) {

    }

    @Override
    public void onRemove() {
        super.onRemove();
    }
}
