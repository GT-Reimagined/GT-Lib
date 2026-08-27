package org.gtreimagined.gtlib.gui.slot

import net.minecraft.world.entity.player.Player
import net.minecraftforge.items.IItemHandler
import org.gtreimagined.gtlib.capability.IGuiHandler
import org.gtreimagined.gtlib.gui.SlotType

class SlotInput(type: SlotType<SlotInput>, tile: IGuiHandler, stackHandler: IItemHandler, index: Int) :
    AbstractSlot<SlotInput>(type, tile, stackHandler, index) {
    override fun mayPickup(player: Player): Boolean {
        return true
    }
}
