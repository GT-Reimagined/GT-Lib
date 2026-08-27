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
    index: Int,
    val isSettable: Boolean
) : AbstractSlot<SlotFake>(type, tile, stackHandler, index), IClickableSlot {
    override fun mayPlace(stack: ItemStack): Boolean {
        return this.isSettable
    }

    override fun mayPickup(playerIn: Player): Boolean {
        return this.isSettable
    }

    override fun getMaxStackSize(stack: ItemStack): Int {
        if (this.isSettable) {
            return 1
        }
        return super.getMaxStackSize(stack)
    }

    override fun remove(amount: Int): ItemStack {
        if (!this.isSettable || this.container !is FakeTrackedItemHandler<*>) return super.remove(amount)
        return MachineItemHandler.extractFromInput(this.container, index, amount, false)
    }

    override fun onQuickCraft(p_75220_1_: ItemStack, p_75220_2_: ItemStack) {
    }

    override fun setChanged() {
    }

    override fun set(stack: ItemStack) {
        super.set(stack)
    }

    override fun safeInsert(stack: ItemStack, slot: Int): ItemStack {
        val copy1 = stack.copy()
        var copy2 = stack.copy()
        if (!copy2.isEmpty && this.mayPlace(copy2)) {
            val itemstack = this.item
            val i = min(min(slot, copy2.count), this.getMaxStackSize(copy2) - itemstack.count)
            if (itemstack.isEmpty) {
                this.set(copy2.split(i))
            }

            return copy1
        } else {
            return copy1
        }
    }

    override fun clickSlot(
        clickedButton: Int,
        clickType: ClickType?,
        playerEntity: Player,
        container: AbstractContainerMenu
    ): ItemStack {
        if (!this.isSettable) return ItemStack.EMPTY
        val playerinventory = playerEntity.inventory
        val itemstack = container.carried.copy()
        if ((clickType == ClickType.PICKUP || clickType == ClickType.SWAP) && (clickedButton == 0 || clickedButton == 1)) {
            val heldStack = container.carried.copy()
            this.set(if (heldStack.isEmpty) ItemStack.EMPTY else Utils.ca(this.getMaxStackSize(heldStack), heldStack))
            this.setChanged()
        }
        return itemstack
    }
}
