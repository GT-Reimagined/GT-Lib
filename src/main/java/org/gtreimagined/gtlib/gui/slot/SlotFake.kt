package org.gtreimagined.gtlib.gui.slot

import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandler
import org.gtreimagined.gtlib.capability.IGuiHandler
import org.gtreimagined.gtlib.capability.item.FakeTrackedItemHandler
import org.gtreimagined.gtlib.capability.machine.MachineItemHandler
import org.gtreimagined.gtlib.gui.SlotType
import org.gtreimagined.gtlib.util.Utils
import kotlin.math.min

class SlotFake(
    type: SlotType<SlotFake>,
    tile: IGuiHandler,
    stackHandler: IItemHandler,
    index: Int
) : AbstractSlot<SlotFake>(type, tile, stackHandler, index) {
    override fun getMaxStackSize(stack: ItemStack): Int {
        if (this.mayPlace(stack)) {
            return 1
        }
        return super.getMaxStackSize(stack)
    }

}
