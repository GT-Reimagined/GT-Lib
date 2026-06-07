package org.gtreimagined.gtlib.gui.slot;

import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.gui.SlotType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;


public class SlotEnergy extends AbstractSlot<SlotEnergy> {
    public SlotEnergy(SlotType<SlotEnergy> type, IGuiHandler tile, IItemHandler itemHandler, int index) {
        super(type, tile, itemHandler, index);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(@NotNull ItemStack stack) {
        return 1;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return true;
    }
}
