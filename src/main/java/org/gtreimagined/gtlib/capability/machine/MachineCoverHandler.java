package org.gtreimagined.gtlib.capability.machine;

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
    public MachineCoverHandler(T tile) {
        super(tile, tile.getValidCovers());
        Arrays.stream(Ref.DIRS).forEach(d -> {
            Direction facing = getTileFacing();
            Direction newDir = Utils.rotate(facing, d);
            CoverFactory factory = tile.getMachineType().defaultCover(d);
            covers.put(newDir, factory.get().get(this, null, newDir, factory));
            buildLookup(ICover.emptyFactory, tile.getMachineType().defaultCover(d), newDir);
        });
    }

    public Direction getOutputFacing() {
        return lookupSingle(getTile().getMachineType().getOutputCover());
    }

    public ICover getOutputCover() {
        return get(lookupSingle(getTile().getMachineType().getOutputCover()));
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
        boolean[] outputCoverOnly = {true};
        covers.forEach((s, cover) -> {
            if (!cover.isEmpty()) { // Don't store EMPTY covers unnecessarily
                if (!isCoverDefault(cover)) outputCoverOnly[0] = false;
                Direction inverseRotated = Utils.rotateInverse(getTileFacing(), s);
                sides[0] |= (1 << inverseRotated.get3DDataValue());
                CoverFactory.writeCover(tag, cover, inverseRotated, true);
            }
        });
        if (!outputCoverOnly[0]){
            tag.putByte(Ref.TAG_MACHINE_COVER_SIDE, sides[0]);
            machine.getOrCreateTag().put("covers", tag);
        }
    }

    protected boolean isCoverDefault(ICover cover){
        return cover.getFactory() == getTile().getMachineType().getOutputCover();
    }

    public boolean setOutputFacing(Player entity, Direction side) {
        Direction dir = getOutputFacing();
        boolean empty = getTile().getMachineType().getOutputCover() == ICover.emptyFactory;
        if (dir == null && empty) return false;
        if (side == dir) return false;
        if (getTileFacing() == side && !getTile().getMachineType().allowsFrontCovers()) return false;
        boolean ok = dir != null ? moveCover(entity, dir, side) : set(side, getTile().getMachineType().getOutputCover().get().get(this, null, side, getTile().getMachineType().getOutputCover()), true);
        if (ok) {
            getTile().invalidateCaps();
        }
        return ok;
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
        return getTile().getMachineType().getOutputCover() != cover.getFactory();
    }

    @Override
    public InteractionResult onInteract(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Direction side, @Nullable GTToolType type) {
        return super.onInteract(player, hand, side, type);
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object... data) {
        covers.forEach((s, c) -> c.onMachineEvent(getTile(), event));
    }

    @Override
    public boolean isValid(@NotNull Direction side, @NotNull ICover replacement) {
        if (!validCovers.contains(replacement.getLoc())) return false;
        if (side == getOutputFacing()) return false;
        return (get(side).isEmpty() && !replacement.isEmpty()) || super.isValid(side, replacement);
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
