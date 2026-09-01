package org.gtreimagined.gtlib.gui

import brachy.modularui.widgets.slot.ModularSlot
import net.minecraft.world.item.ItemStack
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.items.wrapper.EmptyHandler
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine
import org.gtreimagined.gtlib.capability.FluidHandler
import org.gtreimagined.gtlib.capability.IGuiHandler
import org.gtreimagined.gtlib.capability.fluid.FluidTanks
import org.gtreimagined.gtlib.cover.ICover
import org.gtreimagined.gtlib.gui.slot.AbstractSlot
import org.gtreimagined.gtlib.gui.slot.SlotCell
import org.gtreimagined.gtlib.gui.slot.SlotEnergy
import org.gtreimagined.gtlib.gui.slot.SlotFake
import org.gtreimagined.gtlib.gui.slot.SlotInput
import org.gtreimagined.gtlib.gui.slot.SlotOutput
import org.gtreimagined.gtlib.mui.GTGuiTextures
import org.gtreimagined.tesseract.api.forge.TesseractCaps
import java.util.function.BiPredicate
import java.util.function.Function

object SlotTypes {

    private val NO_INPUT: BiPredicate<IGuiHandler, ItemStack> = BiPredicate { _, _ -> false }

    private val ITEM_IN_PRED: BiPredicate<IGuiHandler, ItemStack> = BiPredicate { g, stack ->
        if (g is BlockEntityMachine<*>) {
            return@BiPredicate g.recipeHandler.map {
                it.accepts(stack)
            }.orElse(true)!!
        }
        true
    }

    @JvmField
    var IT_IN: SlotType<SlotInput> = SlotType.create { b ->
        b.id = "item_in"
        b.slotSupplier = SlotType.ISlotSupplier { type, gui, inv, i, _ ->
            SlotInput(
                type, gui,
                inv.getOrDefault(type, EmptyHandler.INSTANCE), i
            )
        }
        b.tester = ITEM_IN_PRED
        b.allowExternalOutput = false
    }

    @JvmField
    val IT_OUT: SlotType<SlotOutput> = SlotType.create { b ->
        b.id = "item_out"
        b.slotSupplier = SlotType.ISlotSupplier { type, gui, inv, i, _ ->
            SlotOutput(
                type, gui,
                inv.getOrDefault(type, EmptyHandler.INSTANCE), i
            )
        }
        b.tester = NO_INPUT
        b.slotGroup = false
        b.allowExternalInput = false
        b.mayPlace = false
    }

    @JvmField
    val DISPLAY: SlotType<SlotFake> = SlotType.create { b ->
        b.id = "display"
        b.slotSupplier = SlotType.ISlotSupplier { type, gui, item, i, d ->
            SlotFake(
                type, gui,
                item.getOrDefault(type, EmptyHandler.INSTANCE), i
            )
        }
        b.tester = NO_INPUT
        b.slotGroup = false
        b.allowExternalInput = false
        b.allowExternalOutput = false
        b.mayPlace = false
        b.mayPickup = false
        b.phantom = true
    }

    @JvmField
    val DISPLAY_SETTABLE: SlotType<SlotFake> = SlotType.create { b ->
        b.id = "display_settable"
        b.slotSupplier = SlotType.ISlotSupplier { type, gui, item, i, _ ->
            SlotFake(
                type,gui,
                item.getOrDefault(type, EmptyHandler.INSTANCE), i
            )
        }
        b.tester = NO_INPUT
        b.slotGroup = false
        b.allowExternalInput = false
        b.allowExternalOutput = false
        b.phantom = true
    }

    @JvmField
    val STORAGE: SlotType<AbstractSlot<*>> = SlotType.create { b ->
        b.id = "storage"
        b.slotSupplier = SlotType.ISlotSupplier { type, gui, item, i, _ ->
            AbstractSlot(
                type, gui,
                item.getOrDefault(type, EmptyHandler.INSTANCE), i
            )
        }
        b.tester = BiPredicate { _, _ -> true }
    }

    @JvmField
    val CELL_IN: SlotType<SlotCell> = SlotType.create { b ->
        b.id = "cell_in"
        b.slotSupplier = SlotType.ISlotSupplier { type, gui, inv, i, _ ->
            SlotCell(
                type, gui,
                inv.getOrDefault(type, EmptyHandler.INSTANCE), i
            )
        }
        b.tester = BiPredicate { _, i -> i.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent }
        b.allowExternalOutput = false
        b.overlay = GTGuiTextures.CELL_IN_SLOT_OVERLAY
    }

    @JvmField
    val CELL_OUT: SlotType<SlotCell> = SlotType.create { b ->
        b.id = "cell_out"
        b.slotSupplier = SlotType.ISlotSupplier { type, gui, inv, i, _ ->
            SlotCell(
                type, gui,
                inv.getOrDefault(type, EmptyHandler.INSTANCE), i
            )
        }
        b.tester = BiPredicate { _, i -> i.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent }
        b.allowExternalInput = false
        b.mayPlace = false
        b.slotGroup = false
        b.overlay = GTGuiTextures.CELL_OUT_SLOT_OVERLAY
    }

    @JvmField
    val ENERGY: SlotType<SlotEnergy> = SlotType.create { b ->
        b.id = "energy"
        b.slotSupplier = SlotType.ISlotSupplier { type, gui, inv, i, _ ->
            SlotEnergy(
                type, gui,
                inv.getOrDefault(type, EmptyHandler.INSTANCE), i
            )
        }
        b.tester = BiPredicate { t, i ->
            if (t is BlockEntityMachine<*>) {
                return@BiPredicate t.energyHandler.map { eh ->
                    i.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).map { inner ->
                        (inner.inputVoltage or inner.outputVoltage) == (eh.inputVoltage or eh.outputVoltage)
                    }.orElse(i.getCapability(ForgeCapabilities.ENERGY).isPresent)
                }.orElse(false) == true
            }
            true
        }
        b.allowExternalOutput = false
        b.overlay = GTGuiTextures.ENERGY_SLOT_OVERLAY
    }

    @JvmField
    val FL_IN: SlotType<ModularSlot> = SlotType.create { b ->
        b.id = "fluid_in"
        b.fluidHandlerSupplier = Function { g ->
            if (g is BlockEntityMachine<*>) {
                return@Function g.fluidHandler.map { it.inputTanks }
                    .orElse(FluidTanks.EMPTY_TANK)!!
            }
            if (g is ICover) {
                if (g.fluidTanks != null) {
                    return@Function g.fluidTanks
                        .getOrDefault(FluidHandler.FluidTankType.INPUT, FluidTanks.EMPTY_TANK)
                }
            }
            FluidTanks.EMPTY_TANK
        }
        b.background = GTGuiTextures.FLUID_SLOT
        b.overlay = GTGuiTextures.FLUID_IN_SLOT_OVERLAY
    }

    //Cheat using same ID to get working counter.
    @JvmField
    val FL_OUT: SlotType<ModularSlot> = SlotType.create { b ->
        b.id = "fluid_out"
        b.fluidHandlerSupplier = Function { g ->
            if (g is BlockEntityMachine<*>) {
                return@Function g.fluidHandler.map<FluidTanks?> { it.outputTanks }
                    .orElse(FluidTanks.EMPTY_TANK)!!
            }
            if (g is ICover) {
                if (g.fluidTanks != null) {
                    return@Function g.fluidTanks
                        .getOrDefault(FluidHandler.FluidTankType.OUTPUT, FluidTanks.EMPTY_TANK)
                }
            }
            FluidTanks.EMPTY_TANK
        }
        b.background = GTGuiTextures.FLUID_SLOT
        b.overlay = GTGuiTextures.FLUID_OUT_SLOT_OVERLAY
    }

    @JvmField
    val FL_PHANTOM: SlotType<ModularSlot> = SlotType.create { b ->
        b.id = "fluid_phantom"
        b.fluidHandlerSupplier = Function { g ->
            if (g is BlockEntityMachine<*>) {
                return@Function g.fluidHandler.map { it.phantomTanks }
                    .orElse(FluidTanks.EMPTY_TANK)!!
            }
            if (g is ICover) {
                if (g.fluidTanks != null) {
                    return@Function g.fluidTanks
                        .getOrDefault(FluidHandler.FluidTankType.PHANTOM, FluidTanks.EMPTY_TANK)
                }
            }
            FluidTanks.EMPTY_TANK
        }
        b.background = GTGuiTextures.FLUID_SLOT
        b.phantom = true
    }

    @JvmStatic
    fun init() {
    }
}