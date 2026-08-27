package org.gtreimagined.gtlib.gui.slot

import net.minecraft.world.entity.player.Player
import net.minecraftforge.items.IItemHandler
import org.gtreimagined.gtlib.capability.IGuiHandler
import org.gtreimagined.gtlib.gui.SlotType

class SlotOutput(type: SlotType<SlotOutput>, tile: IGuiHandler, stackHandler: IItemHandler, index: Int) :
    AbstractSlot<SlotOutput>(type, tile, stackHandler, index) {
    override fun mayPickup(player: Player): Boolean {
        return true
    }
}
