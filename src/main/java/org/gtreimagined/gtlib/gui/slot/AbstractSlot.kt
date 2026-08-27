package org.gtreimagined.gtlib.gui.slot

import brachy.modularui.widgets.slot.ModularSlot
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandler
import org.gtreimagined.gtlib.capability.IFilterableHandler
import org.gtreimagined.gtlib.capability.IGuiHandler
import org.gtreimagined.gtlib.capability.item.TrackedItemHandler
import org.gtreimagined.gtlib.capability.machine.MachineItemHandler
import org.gtreimagined.gtlib.gui.SlotType

open class AbstractSlot<T : ModularSlot>(
    val type: SlotType<T>,
    protected val holder: IGuiHandler,
    private val itemHandler: IItemHandler,
    protected val index: Int
) : ModularSlot(
    itemHandler,
    index
) {
    override fun onQuickCraft(oldStackIn: ItemStack, newStackIn: ItemStack) {
        super.onQuickCraft(oldStackIn, newStackIn)
        if (this.itemHandler is TrackedItemHandler<*>) {
            itemHandler.onContentsChanged(this.index)
        }
    }

    override fun setChanged() {
        super.setChanged()
        if (this.itemHandler is TrackedItemHandler<*>) {
            itemHandler.onContentsChanged(this.index)
        }
    }


    override fun remove(amount: Int): ItemStack {
        return MachineItemHandler.extractFromInput(this.itemHandler, index, amount, false)
    }

    override fun mayPickup(playerIn: Player): Boolean {
        return !MachineItemHandler.extractFromInput(this.itemHandler, index, 1, true).isEmpty && type.mayPickup
    }

    override fun mayPlace(stack: ItemStack): Boolean {
        var filter = true
        if (this.holder is IFilterableHandler) {
            filter = holder.test(type, index, stack)
        }
        return filter && this.type.tester.test(this.holder, stack) && type.mayPlace
    }
}
