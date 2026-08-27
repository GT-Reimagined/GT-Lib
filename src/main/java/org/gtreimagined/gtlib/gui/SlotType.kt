package org.gtreimagined.gtlib.gui

import brachy.modularui.drawable.UITexture
import brachy.modularui.widgets.slot.ModularSlot
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandler
import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.capability.IGuiHandler
import org.gtreimagined.gtlib.capability.fluid.FluidTanks
import org.gtreimagined.gtlib.machine.event.IMachineEvent
import org.gtreimagined.gtlib.mui.GTGuiTextures
import org.gtreimagined.gtlib.registration.IGTObject
import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.function.Function

@JvmRecord
data class SlotType<T : ModularSlot>(
    val id: String,
    @JvmField val slotSupplier: ISlotSupplier<T>?,
    @JvmField val fluidHandlerSupplier: Function<IGuiHandler, FluidTanks>?,
    @JvmField val mayPickup: Boolean, @JvmField val mayPlace: Boolean, @JvmField val allowExternalOutput: Boolean,
    @JvmField val allowExternalInput: Boolean, @JvmField val phantom: Boolean, @JvmField val slotGroup: Boolean,
    @JvmField val background: UITexture?, @JvmField val overlay: UITexture?,
    @JvmField val tester: BiPredicate<IGuiHandler, ItemStack>?
) : IGTObject, IMachineEvent {
    init {
        require(!(slotSupplier == null && fluidHandlerSupplier == null)) { "Slot Type must have either a fluid handler supplier or item slot supplier!" }
    }

    override fun getId(): String {
        return id
    }

    fun interface ISlotSupplier<T : ModularSlot> {
        fun get(type: SlotType<T>, tile: IGuiHandler, slots: Map<SlotType<*>, IItemHandler>, index: Int, data: SlotData<T>): T
    }

    companion object {
        @JvmStatic fun <T : ModularSlot> create(consumer: Consumer<SlotTypeBuilder<T>>): SlotType<T>  = SlotTypeBuilder.create(consumer)
    }

    class SlotTypeBuilder<T : ModularSlot> {
        var id: String? = null
        var slotSupplier: ISlotSupplier<T>? = null
        var fluidHandlerSupplier: Function<IGuiHandler, FluidTanks>? = null
        var mayPickup = true
        var mayPlace = true
        var allowExternalOutput = true
        var allowExternalInput = true
        var phantom = false
        var slotGroup = true
        var background: UITexture? = GTGuiTextures.ITEM_SLOT
        var overlay: UITexture? = null
        var tester = BiPredicate { g: IGuiHandler, i: ItemStack -> true }

        fun id(id: String): SlotTypeBuilder<T>  = apply { this.id = id }
        fun slotSupplier(supplier: ISlotSupplier<T>?): SlotTypeBuilder<T> = apply {this.slotSupplier = supplier}
        fun fluidHandlerSupplier(function: Function<IGuiHandler, FluidTanks>) = apply { this.fluidHandlerSupplier = function }
        fun mayPickup(mayPickup: Boolean) = apply { this.mayPickup = mayPickup }
        fun mayPlace(mayPlace: Boolean) = apply { this.mayPlace = mayPlace }
        fun allowExternalInput(allowExternalInput: Boolean) = apply { this.allowExternalInput = allowExternalInput }
        fun allowExternalOutput(allowExternalOutput: Boolean) = apply { this.allowExternalOutput = allowExternalOutput }
        fun phantom(phantom: Boolean) = apply { this.phantom = phantom }
        fun slotGroup(slotGroup: Boolean) = apply { this.slotGroup = slotGroup }
        fun background(background: UITexture) = apply { this.background = background }
        fun overlay(overlay: UITexture) = apply { this.overlay = overlay }

        private fun build(id: String): SlotType<T> {
            return SlotType(
                id,
                slotSupplier,
                fluidHandlerSupplier,
                mayPickup,
                mayPlace,
                allowExternalOutput,
                allowExternalInput,
                phantom,
                slotGroup,
                background,
                overlay,
                tester
            )
        }
        companion object {
            fun <T : ModularSlot> create(consumer: Consumer<SlotTypeBuilder<T>>): SlotType<T> {
                val b = SlotTypeBuilder<T>()
                consumer.accept(b)
                val id = requireNotNull(b.id) { "Missing id for slot type" }
                val slotType = b.build(id)
                GTAPI.register(SlotType::class.java, slotType)
                return slotType
            }
        }
    }
}
