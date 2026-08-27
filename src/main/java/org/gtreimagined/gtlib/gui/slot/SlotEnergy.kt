package org.gtreimagined.gtlib.gui.slot

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandler
import org.gtreimagined.gtlib.capability.IGuiHandler
import org.gtreimagined.gtlib.gui.SlotType

class SlotEnergy(type: SlotType<SlotEnergy>?, tile: IGuiHandler?, itemHandler: IItemHandler?, index: Int) :
    AbstractSlot<SlotEnergy?>(type, tile, itemHandler, index) {
    override fun getMaxStackSize(): Int {
        return 1
    }

    override fun getMaxStackSize(stack: ItemStack): Int {
        return 1
    }

    override fun mayPickup(playerIn: Player): Boolean {
        return true
    }
}
