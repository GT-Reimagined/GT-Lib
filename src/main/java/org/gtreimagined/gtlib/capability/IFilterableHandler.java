package org.gtreimagined.gtlib.capability;

import org.gtreimagined.gtlib.gui.SlotType;
import net.minecraft.world.item.ItemStack;

public interface IFilterableHandler {
    boolean test(SlotType<?> slotType, int slot, ItemStack stack);
}
