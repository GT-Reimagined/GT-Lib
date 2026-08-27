package org.gtreimagined.gtlib.gui.slot

import net.minecraftforge.items.IItemHandler
import org.gtreimagined.gtlib.capability.IGuiHandler
import org.gtreimagined.gtlib.gui.SlotType

class SlotCell(type: SlotType<SlotCell>, tile: IGuiHandler, stackHandler: IItemHandler, index: Int) :
    AbstractSlot<SlotCell>(type, tile, stackHandler, index)
