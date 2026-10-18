package org.gtreimagined.gtlib.structure;

import com.gtnewhorizon.structurelib.structure.IStructureElement;
import org.gtreimagined.gtlib.block.BlockFakeTile;
import org.gtreimagined.gtlib.blockentity.BlockEntityFakeBlock;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityBasicMultiMachine;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.List;

/**
 * FakeTileElement represents a fake block for this multiblock. It takes on the
 * appearance of another block
 * as well as rendering possible covers on it. It also forwards capability calls
 * to the master controller.
 */
public class FakeTileElement<T extends BlockEntityBasicMultiMachine<T>> implements IStructureElement<T> {

    private IBlockStatePredicate pred;
    private BlockState state;
    private final EnumMap<Direction, CoverFactory> covers = new EnumMap<>(Direction.class);

    public FakeTileElement(BlockState state, IBlockStatePredicate pred) {
        this.state = state;
        this.pred = pred;
    }

    public FakeTileElement(Block block) {
        this(block.defaultBlockState(), ((reader, pos, state1) -> state1.getBlock() == block));
    }

    public FakeTileElement(BlockState state) {
        this(state, (reader, pos, state1) -> state1 == state);
    }

    public FakeTileElement<T> cover(Direction side, CoverFactory cover) {
        this.covers.put(side, cover);
        return this;
    }

    @Override
    public void onStructureSuccess(T machine, Level world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState blockState = world.getBlockState(pos);
        if (blockState.hasProperty(BlockFakeTile.HAS_BE) && !blockState.getValue(BlockFakeTile.HAS_BE)){
            world.setBlock(pos, blockState.setValue(BlockFakeTile.HAS_BE, true), 3);
        }
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof BlockEntityFakeBlock fakeBlock && StructureCache.refCount(world, pos) == 0){
            fakeBlock.setFacing(machine.getFacing()).setCovers(covers);
            fakeBlock.setController(machine);
        }
    }

    @Override
    public void onStructureFail(T basicMultiMachine, Level world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (world.isLoaded(pos)){
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof BlockEntityFakeBlock fakeBlock){
                fakeBlock.setController(null);
            }
            BlockState blockState = world.getBlockState(pos);
            if (blockState.hasProperty(BlockFakeTile.HAS_BE) && blockState.getValue(BlockFakeTile.HAS_BE)){
                world.setBlock(pos, blockState.setValue(BlockFakeTile.HAS_BE, false), 3);
            }
        }
    }

    public void onInfoTooltip(List<Component> text, long count, BlockEntityBasicMultiMachine<?> machine) {
        //super.onInfoTooltip(text, count, machine);
        text.add(Utils.literal("Element replaced with a TileEntity to allow input/output."));
    }

    @Override
    public boolean check(T machine, Level world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = world.getBlockState(pos);
        if (pred.evaluate(machine.getLevel(), pos, state)) {
            boolean defaultReturn = state.hasProperty(BlockFakeTile.HAS_BE);
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof BlockEntityFakeBlock fakeBlock) {
                return fakeBlock.getController() == machine || StructureCache.refCount(world, pos) == 0;
            }
            return defaultReturn;
        }
        return false;
    }

    @Override
    public boolean spawnHint(T basicMultiMachine, Level world, int x, int y, int z, ItemStack trigger) {
        return false;
    }

    @Override
    public boolean placeBlock(T basicMultiMachine, Level world, int x, int y, int z, ItemStack trigger) {
        if (state != null){
            world.setBlock(new BlockPos(x, y, z), state, 3);
            return true;
        }
        return false;
    }
}
