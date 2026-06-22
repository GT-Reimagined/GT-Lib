package org.gtreimagined.gtlib.blockentity;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.BlockFakeTile;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityBasicMultiMachine;
import org.gtreimagined.gtlib.capability.ICoverHandler;
import org.gtreimagined.gtlib.client.dynamic.DynamicTexturer;
import org.gtreimagined.gtlib.client.dynamic.DynamicTexturers;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.network.GTLibNetwork;
import org.gtreimagined.gtlib.network.packets.FakeTilePacket;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BlockEntityFakeBlock extends BlockEntityTickable<BlockEntityFakeBlock> {

    @Getter
    private BlockEntityBasicMultiMachine<?> controller = null;
    public Map<Direction, ICover> covers = new EnumMap<>(Direction.class);
    public Direction facing;
    private BlockPos controllerPos = null;

    public final Map<Direction, DynamicTexturer<ICover, ICover.DynamicKey>> coverTexturer;

    public BlockEntityFakeBlock(BlockPos pos, BlockState state) {
        super(BlockFakeTile.TYPE, pos, state);
        coverTexturer = new Object2ObjectOpenHashMap<>();
    }

    public void setController(BlockEntityBasicMultiMachine<?> controller) {
        this.controller = controller;
        if (level != null) {
            if (controller != null && !level.isClientSide){
                GTLibNetwork.NETWORK.sendToAllLoaded(new FakeTilePacket(this.getBlockPos(), controller.getBlockPos()), level, this.getBlockPos());
            }
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            sidedSync(true);
        }
    }

    @Override
    public void onFirstTickClient(Level level, BlockPos pos, BlockState state) {
        super.onFirstTickClient(level, pos, state);
        if (controllerPos != null) {
            if (level.getBlockEntity(controllerPos) instanceof BlockEntityBasicMultiMachine<?> basicMultiMachine && basicMultiMachine.allowsFakeTiles()) {
                setController(basicMultiMachine);
                controllerPos = null;
            }
        }
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        covers.forEach((s, c) -> {
            if (c.ticks()) {
                c.onTickPost();
            }
        });
    }

    public ICover[] covers() {
        ICover[] ret = new ICover[6];
        for (Direction dir : Ref.DIRS) {
            ICover c = this.covers.get(dir);
            ret[dir.get3DDataValue()] = c == null ? ICover.empty : c;
        }
        return ret;
    }

    public void removeController(BlockEntityBasicMultiMachine<?> controller) {
        if (level != null)
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        this.controller = null;
    }

    public BlockEntityFakeBlock setCovers(Map<Direction, CoverFactory> covers) {
        ICoverHandler<?> handler = ICoverHandler.empty(this);
        for (Map.Entry<Direction, CoverFactory> entry : covers.entrySet()) {
            Direction dir = entry.getKey();
            CoverFactory factory = entry.getValue();
            Direction rot = Utils.coverRotateFacing(dir, facing);
            if (rot.getAxis() == Axis.X) rot = rot.getOpposite();
            ICover cover = factory.get().get(handler, null, rot, factory);
            this.covers.put(
                rot,
                    cover);
        }
        setChanged();
        return this;
    }

    public BlockEntityFakeBlock setFacing(Direction facing) {
        this.facing = facing;
        setChanged();
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public DynamicTexturer<ICover, ICover.DynamicKey> getTexturer(Direction side) {
        return coverTexturer.computeIfAbsent(side,
                dir -> new DynamicTexturer<>(DynamicTexturers.COVER_DYNAMIC_TEXTURER));
    }

    @Nullable
    public ICover getCover(Direction side) {
        return covers.get(side);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains("F")) {
            this.facing = Direction.from3DDataValue(nbt.getInt("F"));
        } else {
            this.facing = Direction.NORTH;
        }
        if (level != null && level.isClientSide) {
            Utils.markTileForRenderUpdate(this);
        }
        this.covers = new EnumMap<>(Direction.class);
        CompoundTag c = nbt.getCompound("C");
        for (Direction dir : Ref.DIRS) {
            ICover cover = CoverFactory.readCover(ICoverHandler.empty(this), dir, c, false);
            if (cover != null)
                covers.put(dir, cover);
        }
        if (nbt.contains("P")) {
            controllerPos = BlockPos.of(nbt.getLong("P"));
            if (level != null && level.isClientSide) {
                if (level.getBlockEntity(controllerPos) instanceof BlockEntityBasicMultiMachine<?> basicMultiMachine && basicMultiMachine.allowsFakeTiles()) {
                    setController(basicMultiMachine);
                    controllerPos = null;
                }
            }
        }
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        writeTag(nbt, true);
        return nbt;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        writeTag(nbt, false);
    }

    private void writeTag(CompoundTag compound, boolean send) {
        if (facing != null) {
            compound.putInt("F", facing.ordinal());
        }
        CompoundTag n = new CompoundTag();
        this.covers.forEach((k, v) -> CoverFactory.writeCover(n, v, v.side(), false));
        compound.put("C", n);
        if (controller != null && send) {
            compound.putLong("P", controller.getBlockPos().asLong());
        }
    }

    public BlockState getState() {
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public List<String> getInfo(boolean simple) {
        List<String> list = super.getInfo(simple);
        if (!simple){
            if (getState() != null)
                list.add("State: " + getState().toString());
            if (facing != null)
                list.add("Facing: " + facing.getName());
            covers.forEach((k, v) -> {
                list.add("Cover on " + k.getName() + ": " + v.getId());
            });
            if (controller != null) {
                list.add("Controller position: "
                        + controller.getBlockPos());
            }
        }
        if (controller != null){
            controller.getInfo(simple);
        }
        return list;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (controller != null){
            return controller.getCapabilityFromFake(cap, side, side == null ? ICover.empty : covers.get(side));
        }
        return super.getCapability(cap, side);
    }
}
