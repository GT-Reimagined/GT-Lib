package muramasa.antimatter.blockentity.pipe;

import lombok.Getter;
import muramasa.antimatter.Ref;
import muramasa.antimatter.blockentity.BlockEntityCache;
import muramasa.antimatter.blockentity.IPreTickTile;
import muramasa.antimatter.capability.Dispatch;
import muramasa.antimatter.capability.FluidHandler;
import muramasa.antimatter.capability.fluid.FluidHandlerNullSideWrapper;
import muramasa.antimatter.capability.fluid.PipeFluidHandlerSidedWrapper;
import muramasa.antimatter.capability.pipe.PipeFluidHandler;
import muramasa.antimatter.cover.ICover;
import muramasa.antimatter.data.AntimatterTags;
import muramasa.antimatter.pipe.PipeSize;
import muramasa.antimatter.pipe.TileTicker;
import muramasa.antimatter.pipe.types.FluidPipe;
import muramasa.antimatter.util.CodeUtils;
import muramasa.antimatter.util.FluidPlatformUtils;
import muramasa.antimatter.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;
import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE;

public class BlockEntityFluidPipe<T extends FluidPipe<T>> extends BlockEntityPipe<T> implements IFluidPipe, IPreTickTile, Dispatch.Sided<IFluidHandler> {

    @Getter
    protected Optional<PipeFluidHandler> fluidHandler;
    public static byte[] SBIT = {1, 2, 4, 8, 16, 32};
    byte[] lastSide;
    int transferredAmount = 0;
    long mTemperature = 293;

    public BlockEntityFluidPipe(T type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        int count = getPipeSize() == PipeSize.QUADRUPLE ? 4 : getPipeSize() == PipeSize.NONUPLE ? 9 : 1;
        fluidHandler = Optional.of(new PipeFluidHandler(this, type.getPressure(getPipeSize()) * 2, count, 0));
        pipeCapHolder.set(() -> this);
        lastSide = new byte[count];
        for (int i = 0; i < count; i++){
            lastSide[i] = 0;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (even(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ())) {
            TileTicker.SERVER_TICK_PRE.add(this);
        } else {
            TileTicker.SERVER_TICK_PR2.add(this);
        }
    }

    @Override
    public void onFirstTick() {
        super.onFirstTick();
    }

    @Override
    public void onBlockUpdate(BlockPos neighbour) {
        super.onBlockUpdate(neighbour);
    }

    @Override
    protected void register() {
    }

    @Override
    protected boolean deregister() {
        return true;
    }


    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(Ref.KEY_MACHINE_FLUIDS))
            fluidHandler.ifPresent(t -> t.deserialize(tag.getCompound(Ref.KEY_MACHINE_FLUIDS)));
        ListTag tags = tag.getList("lastSide", Tag.TAG_BYTE);
        for (int i = 0; i < tags.size(); i++){
            lastSide[i] = ((ByteTag)tags.get(i)).getAsByte();
        }
        mTemperature = tag.getLong("temperature");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        fluidHandler.ifPresent(t -> tag.put(Ref.KEY_MACHINE_FLUIDS, t.serialize(new CompoundTag())));
        ListTag tags = new ListTag();
        for (int i = 0; i < lastSide.length; i++){
            tags.add(ByteTag.valueOf(lastSide[i]));
        }
        tag.put("lastSide", tags);
        tag.putLong("temperature", mTemperature);
    }

    @Override
    public void onRemove() {
        fluidHandler.ifPresent(FluidHandler::onRemove);
        TileTicker.SERVER_TICK_PR2.remove(this);
        TileTicker.SERVER_TICK_PRE.remove(this);
        super.onRemove();
    }


    @Override
    public boolean isGasProof() {
        return getPipeType().isGasProof();
    }

    @Override
    public int getCapacity() {
        return 1;
    }

    @Override
    public long getPressure() {
        return getPipeType().getPressure(getPipeSize());
    }

    @Override
    public int getTemperature() {
        return getPipeType().getMaxTemperature();
    }

    @SuppressWarnings("ConstantValue")
    public long getCurrentTemperature(){
        return fluidHandler.map(f -> {
            long currentTemp = -1;
            for (int i = 0; i < f.getTanks(); i++){
                FluidStack fluid = f.getFluidInTank(i);
                if (fluid.isEmpty()){
                    continue;
                }
                currentTemp = Math.max(FluidPlatformUtils.INSTANCE.getFluidTemperature(fluid.getFluid()), currentTemp);
            }
            return currentTemp == -1 ? 293L : currentTemp;
        }).orElse(293L);
    }

    @Override
    public boolean connects(Direction direction) {
        return canConnect(direction.get3DDataValue());
    }

    @Override
    public boolean validate(Direction dir) {
        if (!super.validate(dir)) return false;
        return BlockEntityCache.getFluidHandlerCached(level, getBlockPos().relative(dir), dir.getOpposite()).isPresent();
    }

    public void setLastSide(Direction lastSide, int tank) {
        this.lastSide[tank] |= SBIT[lastSide.get3DDataValue()];
    }

    @Override
    protected void serverTick(Level level, BlockPos pos, BlockState state) {
        super.serverTick(level, pos, state);
    }

    private boolean mHasToAddTimer = true;

    @Override
    public void onUnregisterPre() {mHasToAddTimer = true;}

    @Override
    public void onServerTickPre(Level level, BlockPos pos, boolean aFirst) {
        transferredAmount = 0;

        IFluidHandler[] adjacentFluidHandlers = new IFluidHandler[6];
        PipeFluidHandler pipeFluidHandler = fluidHandler.orElse(null);
        if (pipeFluidHandler == null) return;

        for (Direction tSide : Direction.values()) {
            if (connects(tSide)) {
                FluidPlatformUtils.getFluidHandler(level, pos.relative(tSide), getCachedBlockEntity(tSide), tSide.getOpposite()).ifPresent(fluidHandler1 -> {
                    adjacentFluidHandlers[tSide.get3DDataValue()] = fluidHandler1;
                });
            }
        }

        boolean tCheckTemperature = true;

        for (int i = 0; i < pipeFluidHandler.getInputTanks().getTanks(); i++){
            FluidTank tTank = pipeFluidHandler.getInputTanks().getTank(i);
            FluidStack tFluid = tTank.getFluid();
            if (!tFluid.isEmpty()){
                mTemperature = (tCheckTemperature ? FluidPlatformUtils.INSTANCE.getFluidTemperature(tFluid.getFluid()) : Math.max(mTemperature, FluidPlatformUtils.INSTANCE.getFluidTemperature(tFluid.getFluid())));
                tCheckTemperature = false;


                if (!isGasProof() && FluidPlatformUtils.INSTANCE.isFluidGaseous(tFluid.getFluid())) {
                    transferredAmount += tTank.drain(Utils.ca(8, tFluid), FluidAction.EXECUTE).getAmount();
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
                    /*try {
                        for (Entity tEntity : (List<Entity>)worldObj.getEntitiesWithinAABB(Entity.class, box(-2, -2, -2, +3, +3, +3))) {
                            UT.Entities.applyTemperatureDamage(tEntity, mTemperature, 2.0F, 10.0F);
                        }
                    } catch(Throwable e) {e.printStackTrace(ERR);}*/
                }

                if (!type.isAcidProof() && tFluid.getFluid().is(AntimatterTags.ACID)){
                    transferredAmount += tTank.drain(Utils.ca(16, tFluid), FluidAction.EXECUTE).getAmount();
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
                    if (level.random.nextInt(100) == 0){
                        tTank.drain(tTank.getFluidAmount(), FluidAction.EXECUTE);
                        level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                        return;
                    }
                }
            }
            if (mTemperature > getTemperature()) {
                burn(level, pos.getX(), pos.getY(), pos.getZ());
                if (level.random.nextInt(100) == 0) {
                    tTank.drain(tTank.getFluidAmount(), FluidAction.EXECUTE);
                    level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                    return;
                }
            }

            if (!tTank.getFluid().isEmpty()) distribute(level, tTank, i, adjacentFluidHandlers);

            lastSide[i] = 0;
        }
    }

    @SuppressWarnings("rawtypes")
    public void distribute(Level level, FluidTank aTank, int i, IFluidHandler[] fluidHandlers) {
        // Check if we are empty.
        if (aTank.isEmpty()) return;
        // Compile all possible Targets into one List.
        List<IFluidHandler> tTanks = new ArrayList<>();
        List<IFluidHandler> tPipes = new ArrayList<>();
        // Amount to check for Distribution
        int tAmount = aTank.getFluid().getAmount();
        // Count all Targets. Also includes THIS for even distribution, thats why it starts at 1.
        int tTargetCount = 1;
        // Put Targets into Lists.
        for (Direction tSide : Direction.values()) {

            // Don't you dare flow backwards!
            if ((lastSide[i] & SBIT[tSide.get3DDataValue()]) != 0) continue;
            // Are we even connected to this Side? (Only gets checked due to the Cover check being slightly expensive)
            if (!connects(tSide)) continue;
            // Covers let distribution happen, right?
            ICover cover = coverHandler.map(c -> c.get(tSide)).orElse(ICover.empty);
            if (!cover.isEmpty() && (cover.blocksOutput(IFluidHandler.class, tSide) || cover.onTransfer(aTank.getFluid().copy(), false, true))) continue;
            // No Tank? Nothing to do then.
            if (fluidHandlers[tSide.get3DDataValue()] == null) continue;
            // Check if the Tank can be filled with this Fluid.
            long insert = fluidHandlers[tSide.get3DDataValue()].fill(Utils.ca(Integer.MAX_VALUE, aTank.getFluid()), SIMULATE);
            if (insert > 0) {
                if (fluidHandlers[tSide.get3DDataValue()] instanceof PipeFluidHandlerSidedWrapper){
                    tPipes.add(level.random.nextInt(tTanks.size()+1), fluidHandlers[tSide.get3DDataValue()]);
                } else {
                    // Add to a random Position in the List.
                    tTanks.add(level.random.nextInt(tTanks.size()+1), fluidHandlers[tSide.get3DDataValue()]);
                }
                // One more Target.
                tTargetCount++;
                // Done everything.
                continue;
            }
        }
        // No Targets? Nothing to do then.
        if (tTargetCount <= 1) return;
        // Amount to distribute normally.
        tAmount = CodeUtils.bindInt(CodeUtils.divup(tAmount, tTargetCount));
        // Distribute to Pipes first.
        for (IFluidHandler tPipe : tPipes) transferredAmount += aTank.drain(Utils.ca(tPipe.fill(Utils.ca(tAmount, aTank.getFluid()), EXECUTE), aTank.getFluid()), EXECUTE).getAmount();
        // Check if we are empty.
        if (aTank.isEmpty()) return;
        // Distribute to Tanks afterwards.
        for (IFluidHandler tTank : tTanks) transferredAmount += aTank.drain(Utils.ca(tTank.fill(Utils.ca(tAmount, aTank.getFluid()), EXECUTE), aTank.getFluid()), EXECUTE).getAmount();
        // Check if we are empty.
        if (aTank.isEmpty()) return;
        // No Targets? Nothing to do then.
        if (tPipes.isEmpty()) return;
        // And then if there still is pressure, distribute to Pipes again.
        tAmount = (aTank.getFluid().getAmount() - aTank.getCapacity()/2) / tPipes.size();
        if (tAmount > 0) for (IFluidHandler tPipe : tPipes) transferredAmount += aTank.drain(Utils.ca(tPipe.fill(Utils.ca(tAmount, aTank.getFluid()), EXECUTE), aTank.getFluid()), EXECUTE).getAmount();
    }

    public static void burn(Level aWorld, int aX, int aY, int aZ) {
        BlockPos pos = new BlockPos(aX, aY, aZ);
        for (Direction tSide : Direction.values()) {
            fire(aWorld, pos.relative(tSide), false);
        }
    }

    public static boolean fire(Level aWorld, BlockPos pos, boolean aCheckFlammability) {
        BlockState tBlock = aWorld.getBlockState(pos);
        if (tBlock.getMaterial() == Material.LAVA || tBlock.getMaterial() == Material.FIRE) return false;
        if (tBlock.getMaterial() == Material.CLOTH_DECORATION || tBlock.getCollisionShape(aWorld, pos).isEmpty()) {
            if (tBlock.getFlammability(aWorld, pos, Direction.NORTH) > 0) return aWorld.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
            if (aCheckFlammability) {
                for (Direction tSide : Direction.values()) {
                    BlockState tAdjacent = aWorld.getBlockState(pos.relative(tSide));
                    if (tAdjacent.getBlock() == Blocks.CHEST || tAdjacent.getBlock() == Blocks.TRAPPED_CHEST) return aWorld.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                    if (tAdjacent.getFlammability(aWorld, pos.relative(tSide), tSide.getOpposite()) > 0) return aWorld.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                }
            } else {
                return aWorld.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
            }
        }
        return false;
    }

    @Override
    public Class<?> getCapClass() {
        return IFluidHandler.class;
    }

    @Override
    public LazyOptional<? extends IFluidHandler> forSide(Direction side) {
        if (fluidHandler.isEmpty()) {
            return LazyOptional.empty();
        }
        if (side == null){
            return LazyOptional.of(() -> new FluidHandlerNullSideWrapper(fluidHandler.get()));
        }
        return LazyOptional.of(() -> new PipeFluidHandlerSidedWrapper(fluidHandler.get(), this, side));
    }

    @Override
    public LazyOptional<? extends IFluidHandler> forNullSide() {
        return forSide(null);
    }

    @Override
    public List<String> getInfo(boolean simple) {
        List<String> list = super.getInfo(simple);
        fluidHandler.ifPresent(t -> {
            for (int i = 0; i < t.getTanks(); i++) {
                FluidStack stack = t.getFluidInTank(i);
                list.add(("Tank " + (i + 1) + ": " + (stack.isEmpty() ? "Empty" : stack.getAmount() + "mb of " + FluidPlatformUtils.INSTANCE.getFluidDisplayName(stack).getString())));
            }
        });
        if (simple) return list;
        list.add("Pressure: " + getPipeType().getPressure(getPipeSize()));
        list.add("Max temperature: " + getPipeType().getMaxTemperature());
        list.add(getPipeType().isGasProof() ? "Gas proof." : "Cannot handle gas.");
        list.add(getPipeType().isAcidProof() ? "Acid proof." : "Cannot handle acids.");
        return list;
    }

    public static boolean even(int... aCoords) {
        int i = 0;
        for (int tCoord : aCoords) {
            if (tCoord % 2 == 0) i++;
        }
        return i % 2 == 0;
    }
}
