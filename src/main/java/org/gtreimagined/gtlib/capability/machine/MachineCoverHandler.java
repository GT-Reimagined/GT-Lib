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

import java.util.Arrays;


public class MachineCoverHandler<T extends BlockEntityMachine<T>> extends CoverHandler<T> implements IMachineHandler, Dispatch.Sided<ICoverHandler<?>> {
    @Getter
    ICover outputCover = ICover.empty;
    //@Getter
    //ICover secondaryOutputCover = ICover.empty;

    public MachineCoverHandler(T tile) {
        super(tile, tile.getValidCovers());
        if (tile.getMachineType().getOutputDir() != null && tile.getMachineType().getOutputCover() != ICover.emptyFactory) {
            Direction rotated = tile.getMachineType().getOutputDir().getRotatedFacing(getTileFacing());
            outputCover = tile.getMachineType().getOutputCover().get().get(this, null, rotated, tile.getMachineType().getOutputCover());
        }
    }

    public Direction getOutputFacing() {
        return outputCover.side();
    }

    @Override
    public ICover[] getAllRendered() {
        ICover[] ret = new ICover[6];
        for (Direction dir : Ref.DIRS) {
            ret[dir.get3DDataValue()] = get(dir);
            if (ret[dir.get3DDataValue()].isEmpty() && outputCover.side() == dir && !outputCover.isEmpty()) {
                ret[dir.get3DDataValue()] = outputCover;
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

    protected boolean isCoverDefault(ICover cover){
        return false;
    }

    public boolean setOutputFacing(Player entity, Direction side) {
        Direction dir = getOutputFacing();
        CoverFactory factory = getTile().getMachineType().getOutputCover();
        boolean empty = factory == ICover.emptyFactory;
        if (dir == null && empty) return false;
        if (side == dir) return false;
        if (getTileFacing() == side && !getTile().getMachineType().allowsOutputCoversOnFacing()) return false;

        ICover copy = factory.get().get(this, outputCover.getTier(), side, factory);
        copy.deserialize(outputCover.serialize());
        outputCover = copy;
        entity.getLevel().playSound(null, getTile().getBlockPos(), Ref.WRENCH, SoundSource.BLOCKS, 1.0f, 1.0f);
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
    protected boolean canRemoveCover(ICover cover) {
        return true;
    }

    @Override
    public void onUpdate() {
       super.onUpdate();
       outputCover.onUpdate();
    }

    @Override
    public void onFirstTick() {
        super.onFirstTick();
        outputCover.onFirstTick();
    }

    @Override
    public void onRemove() {
        super.onRemove();
        outputCover.onRemove();
    }

    public boolean onTransfer(Object obj, Direction side, boolean inputSide, boolean simulate) {
        boolean b = this.get(side).onTransfer(obj, inputSide, simulate);
        return b || (outputCover.side() == side && outputCover.onTransfer(obj, inputSide, simulate));
    }

    @Override
    public InteractionResult onInteract(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Direction side, @Nullable GTToolType type) {
        InteractionResult interactionResult = super.onInteract(player, hand, side, type);
        if (interactionResult == InteractionResult.PASS) interactionResult = outputCover.onInteract(player, hand, side, type);
        return interactionResult;
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object... data) {
        covers.forEach((s, c) -> c.onMachineEvent(getTile(), event));
        outputCover.onMachineEvent(getTile(), event);
    }

    @Override
    public boolean isValid(@NotNull Direction side, @NotNull ICover replacement) {
        if (!validCovers.contains(replacement.getLoc())) return false;
        return (get(side).isEmpty() && !replacement.isEmpty()) || super.isValid(side, replacement);
    }

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        CompoundTag tag = super.serialize(nbt);
        if (!outputCover.isEmpty()){
            CompoundTag output = new CompoundTag();
            CoverFactory.writeCover(output, outputCover, outputCover.side(), false);
            tag.put("outputCover", output);
        }
        return tag;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        byte sides = nbt.getByte(Ref.TAG_MACHINE_COVER_SIDE);
        boolean foundOut = false;
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
