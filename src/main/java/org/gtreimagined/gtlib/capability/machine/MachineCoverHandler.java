package org.gtreimagined.gtlib.capability.machine;

import lombok.Getter;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.CoverHandler;
import org.gtreimagined.gtlib.capability.Dispatch;
import org.gtreimagined.gtlib.capability.ICoverHandler;
import org.gtreimagined.gtlib.capability.IMachineHandler;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.machine.event.IMachineEvent;
import org.gtreimagined.gtlib.tool.GTToolType;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class MachineCoverHandler<T extends BlockEntityMachine<T>> extends CoverHandler<T> implements IMachineHandler, Dispatch.Sided<ICoverHandler<?>> {
    @Getter
    ICover outputCover = ICover.empty;
    @Getter
    ICover secondaryOutputCover = ICover.empty;

    public MachineCoverHandler(T tile) {
        super(tile, tile.getValidCovers());
        if (tile.getMachineType().getOutputDir() != null && tile.getMachineType().getOutputCover() != ICover.emptyFactory) {
            Direction rotated = tile.getMachineType().getOutputDir().getRotatedFacing(getTileFacing());
            outputCover = tile.getMachineType().getOutputCover().get().get(this, null, rotated, tile.getMachineType().getOutputCover());
        }
        if (tile.getMachineType().getSecondaryOutputDir() != null && tile.getMachineType().getSecondaryOutputCover() != ICover.emptyFactory) {
            Direction rotated = tile.getMachineType().getSecondaryOutputDir().getRotatedFacing(getTileFacing());
            secondaryOutputCover = tile.getMachineType().getSecondaryOutputCover().get().get(this, null, rotated, tile.getMachineType().getSecondaryOutputCover());
        }
    }

    public Direction getOutputFacing() {
        return outputCover.side();
    }

    public Direction getSecondaryOutputFacing() {
        return secondaryOutputCover.side();
    }

    @Override
    public ICover[] getAllRendered() {
        ICover[] ret = new ICover[6];
        for (Direction dir : Ref.DIRS) {
            ret[dir.get3DDataValue()] = get(dir);
            if (ret[dir.get3DDataValue()].isEmpty()) {
                if (outputCover.side() == dir && !outputCover.isEmpty()) {
                    ret[dir.get3DDataValue()] = outputCover;
                } else if (secondaryOutputCover.side() == dir && !secondaryOutputCover.isEmpty()) {
                    ret[dir.get3DDataValue()] = secondaryOutputCover;
                }
            }
        }
        return ret;
    }

    public void readFromStack(ItemStack stack){
        if (stack.getTag() != null && stack.getTag().contains("covers")){
            CompoundTag nbt = stack.getTag().getCompound("covers");
            byte sides = nbt.getByte(Ref.TAG_MACHINE_COVER_SIDE);
            for (Direction dir : Direction.values()){
                covers.put(dir, ICover.empty);
            }
            for (int i = 0; i < Ref.DIRS.length; i++) {
                if ((sides & (1 << i)) > 0) {
                    Direction rotated = Utils.rotate(getTileFacing(), Ref.DIRS[i]);
                    ICover cover = CoverFactory.readCoverRotated(this, Direction.from3DDataValue(i), rotated, nbt);
                    if (cover.getFactory() == getTile().getMachineType().getOutputCover()){
                        outputCover = cover;
                        cover = ICover.empty;
                    }
                    if (cover.getFactory() == getTile().getMachineType().getSecondaryOutputCover()){
                        secondaryOutputCover = cover;
                        cover = ICover.empty;
                    }
                    buildLookup(covers.get(rotated).getFactory(), cover.getFactory(), rotated);
                    covers.put(rotated, cover);
                }
            }
            this.getTile().sidedSync(true);
        }
    }

    public void writeToStack(ItemStack machine){
        CompoundTag tag = new CompoundTag();
        byte[] sides = new byte[1];
        covers.forEach((s, cover) -> {
            if (!cover.isEmpty()) { // Don't store EMPTY covers unnecessarily
                Direction inverseRotated = Utils.rotateInverse(getTileFacing(), s);
                sides[0] |= (1 << inverseRotated.get3DDataValue());
                CoverFactory.writeCover(tag, cover, inverseRotated, true);
            }
        });
        if (!tag.isEmpty()) {
            tag.putByte(Ref.TAG_MACHINE_COVER_SIDE, sides[0]);
            machine.getOrCreateTag().put("covers", tag);
        }
    }

    public boolean setOutputFacing(Player entity, Direction side) {
        Direction dir = getOutputFacing();
        CoverFactory factory = getTile().getMachineType().getOutputCover();
        boolean empty = factory == ICover.emptyFactory;
        if (dir == null && empty) return false;
        if (side == dir) return false;
        if (getTileFacing() == side && !getTile().getMachineType().allowsOutputCoversOnFacing()) return false;
        if (!secondaryOutputCover.isEmpty() && getSecondaryOutputFacing() == side) return switchOutputs(entity, side);
        ICover copy = factory.get().get(this, outputCover.getTier(), side, factory);
        copy.deserialize(outputCover.serialize());
        outputCover = copy;
        entity.level().playSound(null, getTile().getBlockPos(), Ref.WRENCH, SoundSource.BLOCKS, 1.0f, 1.0f);
        sync();
        if (getTile().getLevel() != null) {
            if (!getTile().getLevel().isClientSide) {
                getTile().invalidateCaps(side);
            } else {
                if (coverTexturer != null) getTexturer(side).invalidate();
            }
        }
        return true;
    }

    public boolean setSecondaryOutputFacing(Player entity, Direction side) {
        Direction dir = getSecondaryOutputFacing();
        CoverFactory factory = getTile().getMachineType().getSecondaryOutputCover();
        boolean empty = factory == ICover.emptyFactory;
        if (dir == null && empty) return false;
        if (side == dir) return false;
        if (getTileFacing() == side && !getTile().getMachineType().allowsOutputCoversOnFacing()) return false;
        if (!outputCover.isEmpty() && getOutputFacing() == side) return switchOutputs(entity, side);
        ICover copy = factory.get().get(this, secondaryOutputCover.getTier(), side, factory);
        copy.deserialize(secondaryOutputCover.serialize());
        secondaryOutputCover = copy;
        entity.level().playSound(null, getTile().getBlockPos(), Ref.WRENCH, SoundSource.BLOCKS, 1.0f, 1.0f);
        sync();
        if (getTile().getLevel() != null) {
            if (!getTile().getLevel().isClientSide) {
                getTile().invalidateCaps(side);
            } else {
                if (coverTexturer != null) getTexturer(side).invalidate();
            }
        }
        return true;
    }

    public boolean switchOutputs(Player entity, Direction side) {
        CoverFactory outputFactory = getTile().getMachineType().getOutputCover();
        CoverFactory secondaryFactory = getTile().getMachineType().getSecondaryOutputCover();
        Direction newOutputDir = getSecondaryOutputFacing();
        Direction newSecondaryOutputDir = getOutputFacing();
        ICover copy = secondaryFactory.get().get(this, outputCover.getTier(), newOutputDir, outputFactory);
        copy.deserialize(outputCover.serialize());
        outputCover = copy;
        ICover secondaryCopy = secondaryFactory.get().get(this, secondaryOutputCover.getTier(), newSecondaryOutputDir, secondaryFactory);
        secondaryCopy.deserialize(secondaryOutputCover.serialize());
        secondaryOutputCover = secondaryCopy;
        entity.level().playSound(null, getTile().getBlockPos(), Ref.WRENCH, SoundSource.BLOCKS, 1.0f, 1.0f);
        sync();
        if (getTile().getLevel() != null) {
            if (!getTile().getLevel().isClientSide) {
                getTile().invalidateCaps(side);
            } else {
                if (coverTexturer != null) getTexturer(side).invalidate();
            }
        }
        return true;
    }

    @Override
    public boolean set(Direction side, ICover old, ICover stack, boolean sync) {
        if (getTileFacing() == side && !getTile().getMachineType().allowsFrontCovers() && !stack.isEmpty()) return false;
        boolean ok = super.set(side, old, stack, sync);
        if (ok && getTile().getLevel() != null) {
            if (!getTile().getLevel().isClientSide) {
                getTile().invalidateCaps(side);
            } else {
                if (coverTexturer != null) getTexturer(side).invalidate();
            }
        }
        return ok;
    }

    @Override
    public void onTickPre() {
        super.onTickPre();
        if (outputCover.ticks()) outputCover.onTickPre();
        if (secondaryOutputCover.ticks()) secondaryOutputCover.onTickPre();
    }

    @Override
    public void onTickPost() {
       super.onTickPost();
       if (outputCover.ticks()) outputCover.onTickPost();
       if (secondaryOutputCover.ticks()) secondaryOutputCover.onTickPost();
    }

    @Override
    public void onClientTick() {
        super.onClientTick();
        if (outputCover.ticks()) outputCover.onClientTick();
        if (secondaryOutputCover.ticks()) secondaryOutputCover.onClientTick();
    }

    @Override
    public void onFirstTick() {
        super.onFirstTick();
        outputCover.onFirstTick();
        secondaryOutputCover.onFirstTick();
    }

    @Override
    public void onBlockUpdate(Direction side) {
        super.onBlockUpdate(side);
        if (side == outputCover.side()) outputCover.onBlockUpdate();
        if (side == secondaryOutputCover.side()) secondaryOutputCover.onBlockUpdate();
    }

    @Override
    public void onBlockUpdateAllSides() {
        super.onBlockUpdateAllSides();
        outputCover.onBlockUpdateAllSides();
        secondaryOutputCover.onBlockUpdateAllSides();
    }

    @Override
    public void onRemove() {
        super.onRemove();
        outputCover.onRemove();
        secondaryOutputCover.onRemove();
    }

    public boolean onTransfer(Object obj, Direction side, boolean inputSide, boolean simulate) {
        boolean b = this.get(side).onTransfer(obj, inputSide, simulate);
        return b || (outputCover.side() == side && outputCover.onTransfer(obj, inputSide, simulate)) || (secondaryOutputCover.side() == side && secondaryOutputCover.onTransfer(obj, inputSide, simulate));
    }

    @Override
    public InteractionResult onInteract(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Direction side, @Nullable GTToolType type) {
        InteractionResult interactionResult = super.onInteract(player, hand, side, type);
        if (interactionResult == InteractionResult.PASS) {
            if (side == outputCover.side()) interactionResult = outputCover.onInteract(player, hand, side, type);
            else if (side == secondaryOutputCover.side()) interactionResult = secondaryOutputCover.onInteract(player, hand, side, type);
        }
        return interactionResult;
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object... data) {
        covers.forEach((s, c) -> c.onMachineEvent(getTile(), event));
        outputCover.onMachineEvent(getTile(), event);
        secondaryOutputCover.onMachineEvent(getTile(), event);
    }

    @Override
    public boolean isValid(@NotNull Direction side, @NotNull ICover replacement) {
        if (!validCovers.contains(replacement.getLoc())) return false;
        return (get(side).isEmpty() && !replacement.isEmpty()) || super.isValid(side, replacement);
    }

    @Override
    public <U> boolean blocksCapability(Class<U> capability, Direction side) {
        return super.blocksCapability(capability, side) || (side == outputCover.side() && outputCover.blocksCapability(capability, side)) || (side == secondaryOutputCover.side() && secondaryOutputCover.blocksCapability(capability, side));
    }

    @Override
    public <U> boolean blocksInput(Class<U> capability, Direction side) {
        return super.blocksInput(capability, side) || (side == outputCover.side() && outputCover.blocksInput(capability, side)) || (side == secondaryOutputCover.side() && secondaryOutputCover.blocksInput(capability, side));
    }

    @Override
    public <U> boolean blocksOutput(Class<U> capability, Direction side) {
        return super.blocksOutput(capability, side) || (side == outputCover.side() && outputCover.blocksOutput(capability, side)) || (side == secondaryOutputCover.side() && secondaryOutputCover.blocksOutput(capability, side));
    }

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        CompoundTag tag = super.serialize(nbt);
        if (!outputCover.isEmpty()){
            CompoundTag output = new CompoundTag();
            CoverFactory.writeCover(output, outputCover, outputCover.side(), false);
            tag.put("outputCover", output);
        }
        if (!secondaryOutputCover.isEmpty()){
            CompoundTag output = new CompoundTag();
            CoverFactory.writeCover(output, secondaryOutputCover, secondaryOutputCover.side(), false);
            tag.put("secondaryOutputCover", output);
        }
        return tag;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        byte sides = nbt.getByte(Ref.TAG_MACHINE_COVER_SIDE);
        boolean foundOut = false, foundSecondaryOut = false;
        for (int i = 0; i < Ref.DIRS.length; i++) {
            if ((sides & (1 << i)) > 0) {
                ICover cover = CoverFactory.readCover(this, Direction.from3DDataValue(i), nbt, false);
                if (cover == null){
                    GTLib.LOGGER.warn(nbt + "at d: " + Direction.from3DDataValue(i) + "at pos: " + getTile().getBlockPos());
                    cover = ICover.empty;
                }
                if (cover.getFactory() == getTile().getMachineType().getOutputCover()) {
                    outputCover = cover;
                    cover = ICover.empty;
                    foundOut = true;
                }
                if (cover.getFactory() == getTile().getMachineType().getSecondaryOutputCover()) {
                    secondaryOutputCover = cover;
                    cover = ICover.empty;
                    foundSecondaryOut = true;
                }
                buildLookup(covers.get(Ref.DIRS[i]).getFactory(), cover.getFactory(), Ref.DIRS[i]);
                covers.put(Ref.DIRS[i], cover);
            } else {
                buildLookup(covers.get(Ref.DIRS[i]).getFactory(), ICover.emptyFactory, Ref.DIRS[i]);
                covers.put(Ref.DIRS[i], ICover.empty);
            }
            if (nbt.contains("outputCover") && !foundOut) {
                ICover outputCover = CoverFactory.readCover(this, Direction.from3DDataValue(i), nbt.getCompound("outputCover"), false);
                if (outputCover != null && !outputCover.isEmpty()){
                    this.outputCover = outputCover;
                    foundOut = true;
                }
            }
            if (nbt.contains("secondaryOutputCover") && !foundSecondaryOut) {
                ICover outputCover = CoverFactory.readCover(this, Direction.from3DDataValue(i), nbt.getCompound("secondaryOutputCover"), false);
                if (outputCover != null && !outputCover.isEmpty()){
                    this.secondaryOutputCover = outputCover;
                    foundSecondaryOut = true;
                }
            }
        }

        Level w = getTile().getLevel();
        if (w != null && w.isClientSide) {
            Utils.markTileForRenderUpdate(this.getTile());
        }
    }

    public Direction getTileFacing() {
        return getTile().getFacing();
    }

    @Override
    public LazyOptional<ICoverHandler<?>> forSide(Direction side) {
        return LazyOptional.of(() -> this);
    }

    @Override
    public LazyOptional<? extends ICoverHandler<?>> forNullSide() {
        return LazyOptional.of(() -> this);
    }
}
