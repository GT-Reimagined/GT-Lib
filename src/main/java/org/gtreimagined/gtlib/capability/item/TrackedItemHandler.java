package org.gtreimagined.gtlib.capability.item;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityHatch;
import org.gtreimagined.gtlib.capability.IFilterableHandler;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.gui.SlotType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;


@Getter
public class TrackedItemHandler<T extends IGuiHandler> extends ItemStackHandler implements ITrackedHandler {

    private final T tile;
    @Accessors(fluent = true)
    private final boolean allowExternalOutput;
    @Accessors(fluent = true)
    private final boolean allowExternalInput;
    private final BiPredicate<IGuiHandler, ItemStack> validator;
    private final int limit;
    private final int size;
    private final SlotType<?> type;

    public TrackedItemHandler(T tile, SlotType<?> type, int size, boolean allowExternalOutput, boolean allowExternalInput, BiPredicate<IGuiHandler, ItemStack> validator) {
        this(tile, type, size, allowExternalOutput, allowExternalInput, validator, 64);
    }

    public TrackedItemHandler(T tile, SlotType<?> type, int size, boolean allowExternalOutput, boolean allowExternalInput, BiPredicate<IGuiHandler, ItemStack> validator, int limit) {
        super(size);
        this.tile = tile;
        this.allowExternalOutput = allowExternalOutput;
        this.allowExternalInput = allowExternalInput;
        this.validator = validator;
        this.limit = limit;
        this.size = size;
        this.type = type;
    }

    public boolean hasSlotDiversity(){
        return this.type == SlotType.IT_IN && !(tile instanceof BlockEntityHatch<?>);
    }

    @Override
    public int getSlotLimit(int slot) {
        return limit;
    }

    @Override
    public void onContentsChanged(int slot) {
        if (tile instanceof BlockEntityMachine<?> machine){
            if (machine.getLevel() == null) return;
            machine.getLevel().blockEntityChanged(machine.getBlockPos());
            machine.onMachineEvent(type, slot);
        } else if (tile instanceof ICover cover){
            if (cover.source().getTile().getLevel() == null) return;
            cover.source().getTile().getLevel().blockEntityChanged(cover.source().getTile().getBlockPos());
            cover.onMachineEvent(cover, type, slot);
        }
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        boolean validate = !(tile instanceof IFilterableHandler filterableHandler) || filterableHandler.test(type, slot, stack);
        validate = validate && validator.test(tile, stack);
        if (!validate)
            return stack;
        /*if (simulate) {

        }*/
        return super.insertItem(slot, stack, simulate);
    }

    @NotNull
    public ItemStack insertOutputItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return super.insertItem(slot, stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return super.extractItem(slot, amount, simulate);
    }

    @NotNull
    public ItemStack extractFromInput(int slot, int amount, boolean simulate) {
        return super.extractItem(slot, amount, simulate);
    }
    //Size is defined by GUI and not the NBT data.
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        nbt.remove("Size");
        return nbt;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;//validator.test(tile, stack);
    }
}
