package org.gtreimagined.gtlib.capability.pipe;

import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityItemPipe;
import org.gtreimagined.gtlib.capability.CoverHandler;
import org.gtreimagined.gtlib.capability.item.SidedCombinedInvWrapper;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

public class PipeItemHandler extends SidedCombinedInvWrapper {
    BlockEntityItemPipe<?> pipe;
    public PipeItemHandler(Direction side, BlockEntityItemPipe<?> pipe, CoverHandler<?> coverHandler, IItemHandlerModifiable... itemHandler) {
        super(side, coverHandler, itemHandler);
        this.pipe = pipe;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        ItemStack superInsert = super.insertItem(slot, stack, simulate);
        if (superInsert.getCount() < stack.getCount() && !simulate){
            pipe.mLastReceivedFrom = (byte) side.get3DDataValue();
        }
        return superInsert;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }
}
