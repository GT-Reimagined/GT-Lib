package muramasa.antimatter.capability.item;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EmptyContainer implements ExtendedItemContainer {
    public static final EmptyContainer INSTANCE = new EmptyContainer();
    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }

    @Override
    public boolean isItemValid(int i, @NotNull ItemStack itemStack) {
        return false;
    }

    @Override
    public int getSlots() {
        return 0;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int index, ItemStack stack) {

    }
}
