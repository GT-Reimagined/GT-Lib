package org.gtreimagined.gtlib.gui.slot;

import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.gui.SlotType;
import net.minecraftforge.items.IItemHandler;

public class SlotCell extends AbstractSlot<SlotCell> {

    public SlotCell(SlotType<SlotCell> type, IGuiHandler tile, IItemHandler stackHandler, int index) {
        super(type, tile, stackHandler, index);
    }

}
