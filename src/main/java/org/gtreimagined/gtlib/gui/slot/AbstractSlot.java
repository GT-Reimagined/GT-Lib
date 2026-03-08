package org.gtreimagined.gtlib.gui.slot;

import brachy.modularui.widgets.slot.ModularSlot;
import lombok.Getter;
import org.gtreimagined.gtlib.capability.IFilterableHandler;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.capability.item.TrackedItemHandler;
import org.gtreimagined.gtlib.capability.machine.MachineItemHandler;
import org.gtreimagined.gtlib.gui.SlotType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;


public class AbstractSlot<T extends Slot> extends ModularSlot {
    protected final int index;
    public final SlotType<T> type;
    protected final IGuiHandler holder;
    private final int x1;
    private final int y1;
    @Getter
    private final IItemHandler container;

    public AbstractSlot(SlotType<T> type, IGuiHandler tile, IItemHandler stackHandler, int index, int x, int y) {
        super(stackHandler, index);
        this.container = stackHandler;
        this.index = index;
        this.type = type;
        this.holder = tile;
        x1 = x;
        this.y1 = y;
    }

    @Override
    public void onQuickCraft(@NotNull ItemStack oldStackIn, @NotNull ItemStack newStackIn) {
        super.onQuickCraft(oldStackIn, newStackIn);
        if (this.container instanceof TrackedItemHandler<?> trackedItemHandler) {
            trackedItemHandler.onContentsChanged(this.index);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.container instanceof TrackedItemHandler<?> trackedItemHandler) {
            trackedItemHandler.onContentsChanged(this.index);
        }
    }


    @Override
    @NotNull
    public ItemStack remove(int amount) {
        return MachineItemHandler.extractFromInput(this.container, index, amount, false);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return !MachineItemHandler.extractFromInput(this.container, index, 1, true).isEmpty();
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        boolean filter = true;
        if (this.holder instanceof IFilterableHandler handler){
            filter = handler.test(type, index, stack);
        }
        return filter && this.type.tester.test(this.holder, stack);
    }
}
