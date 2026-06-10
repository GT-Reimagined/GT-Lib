package org.gtreimagined.gtlib.gui.slot;

import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.capability.item.FakeTrackedItemHandler;
import org.gtreimagined.gtlib.capability.machine.MachineItemHandler;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;


public class SlotFake extends AbstractSlot<SlotFake> implements IClickableSlot {
    final boolean settable;

    public SlotFake(SlotType<SlotFake> type, IGuiHandler tile, IItemHandler stackHandler, int index, boolean settable) {
        super(type, tile, stackHandler, index);
        this.settable = settable;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return settable;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return settable;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        if (settable) {
            return 1;
        }
        return super.getMaxStackSize(stack);
    }

    @Override
    @NotNull
    public ItemStack remove(int amount) {
        if (!settable || !(this.getContainer() instanceof FakeTrackedItemHandler)) return super.remove(amount);
        return MachineItemHandler.extractFromInput(this.getContainer(), index, amount, false);
    }

    @Override
    public void onQuickCraft(@NotNull ItemStack p_75220_1_, @NotNull ItemStack p_75220_2_) {
    }

    @Override
    public void setChanged() {

    }

    @Override
    public void set(@NotNull ItemStack stack) {
        super.set(stack);
    }

    @Override
    public ItemStack safeInsert(ItemStack p_150657_, int p_150658_) {
        ItemStack st = p_150657_.copy();
        p_150657_ = st.copy();
        if (!p_150657_.isEmpty() && this.mayPlace(p_150657_)) {
           ItemStack itemstack = this.getItem();
           int i = Math.min(Math.min(p_150658_, p_150657_.getCount()), this.getMaxStackSize(p_150657_) - itemstack.getCount());
           if (itemstack.isEmpty()) {
              this.set(p_150657_.split(i));
           }
  
           return st;
        } else {
           return st;
        }
     }

    public boolean isSettable() {
        return settable;
    }

    public ItemStack clickSlot(int clickedButton, ClickType clickType, Player playerEntity, AbstractContainerMenu container) {
        if (!settable) return ItemStack.EMPTY;
        Inventory playerinventory = playerEntity.getInventory();
        ItemStack itemstack = container.getCarried().copy();
        if ((clickType == ClickType.PICKUP || clickType == ClickType.SWAP) && (clickedButton == 0 || clickedButton == 1)) {
            ItemStack heldStack = container.getCarried().copy();
            this.set(heldStack.isEmpty() ? ItemStack.EMPTY : Utils.ca(this.getMaxStackSize(heldStack), heldStack));
            this.setChanged();
        }
        return itemstack;
    }
}
