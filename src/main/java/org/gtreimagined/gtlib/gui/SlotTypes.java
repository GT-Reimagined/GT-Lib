package org.gtreimagined.gtlib.gui;

import brachy.modularui.widgets.slot.ModularSlot;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.FluidHandler;
import org.gtreimagined.gtlib.capability.FluidHandler.FluidTankType;
import org.gtreimagined.gtlib.capability.fluid.FluidTanks;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.gui.SlotType.ItIn;
import org.gtreimagined.gtlib.gui.slot.AbstractSlot;
import org.gtreimagined.gtlib.gui.slot.SlotCell;
import org.gtreimagined.gtlib.gui.slot.SlotEnergy;
import org.gtreimagined.gtlib.gui.slot.SlotFake;
import org.gtreimagined.gtlib.gui.slot.SlotInput;
import org.gtreimagined.gtlib.gui.slot.SlotOutput;
import org.gtreimagined.gtlib.mui.GTGuiTextures;
import org.gtreimagined.tesseract.api.forge.TesseractCaps;

public class SlotTypes {
    public static SlotType<SlotInput> IT_IN = SlotType.<SlotInput>builder().id("item_in")
            .slotSupplier((type, gui, inv, i, d) -> new SlotInput(type, gui, inv.getOrDefault(type, new EmptyHandler()), i))
            .tester(new ItIn()).allowExternalOutput(false).build();
    public static SlotType<SlotOutput> IT_OUT = SlotType.<SlotOutput>builder().id("item_out")
            .slotSupplier((type, gui, inv, i, d) -> new SlotOutput(type, gui, inv.getOrDefault(type, new EmptyHandler()), i))
            .tester(SlotType.NO_INPUT).slotGroup(false).allowExternalInput(false).mayPlace(false).build();
    public static SlotType<SlotFake> DISPLAY = SlotType.<SlotFake>builder().id("display")
            .slotSupplier((type, gui, item, i, d) -> new SlotFake(type, gui, item.getOrDefault(type, new EmptyHandler()), i, false))
            .tester(SlotType.NO_INPUT).slotGroup(false).allowExternalInput(false).allowExternalOutput(false).mayPlace(false).mayPickup(false).phantom(true).build();
    public static SlotType<SlotFake> DISPLAY_SETTABLE = SlotType.<SlotFake>builder().id("display_settable")
            .slotSupplier((type, gui, item, i, d) -> new SlotFake(type, gui, item.getOrDefault(type, new EmptyHandler()), i, true))
            .tester(SlotType.NO_INPUT).slotGroup(false).allowExternalInput(false).allowExternalOutput(false).phantom(true).build();
    public static SlotType<AbstractSlot<?>> STORAGE = SlotType.<AbstractSlot<?>>builder().id("storage").slotSupplier((type, gui, item, i, d) -> new AbstractSlot<>(type, gui, item.getOrDefault(type, new EmptyHandler()), i))
            .tester((t, i) -> true).build();
    public static SlotType<SlotCell> CELL_IN = SlotType.<SlotCell>builder().id("cell_in").slotSupplier((type, gui, inv, i, d) -> new SlotCell(type, gui, inv.getOrDefault(type, new EmptyHandler()), i))
            .tester((t, i) -> i.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent())
            .allowExternalOutput(false).overlay(GTGuiTextures.CELL_IN_SLOT_OVERLAY).build();
    public static SlotType<SlotCell> CELL_OUT = SlotType.<SlotCell>builder().id("cell_out").slotSupplier((type, gui, inv, i, d) -> new SlotCell(type, gui, inv.getOrDefault(type, new EmptyHandler()), i))
            .tester((t, i) -> i.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent())
            .allowExternalInput(false).mayPlace(false).slotGroup(false).overlay(GTGuiTextures.CELL_OUT_SLOT_OVERLAY).build();
    public static SlotType<SlotEnergy> ENERGY = SlotType.<SlotEnergy>builder().id("energy").slotSupplier((type, gui, inv, i, d) -> new SlotEnergy(type, gui, inv.getOrDefault(type, new EmptyHandler()), i))
            .tester((t, i) -> {
                if (t instanceof BlockEntityMachine<?> machine) {
                    return machine.energyHandler.map(eh -> {
                        return i.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).map(inner -> {
                            return ((inner.getInputVoltage() | inner.getOutputVoltage()) == (eh.getInputVoltage() | eh.getOutputVoltage()));
                        }).orElse(i.getCapability(ForgeCapabilities.ENERGY).isPresent());
                    }).orElse(false);
                }
                return true;
            }).allowExternalOutput(false).overlay(GTGuiTextures.ENERGY_SLOT_OVERLAY).build();
    public static SlotType<ModularSlot> FL_IN = SlotType.builder().id("fluid_in").fluidHandlerSupplier(g -> {
        if (g instanceof BlockEntityMachine<?> machine) {
            return machine.fluidHandler.map(FluidHandler::getInputTanks).orElse(FluidTanks.EMPTY_TANK);
        }
        if (g instanceof ICover cover){
            if (cover.getFluidTanks() != null){
                return cover.getFluidTanks().getOrDefault(FluidTankType.INPUT, FluidTanks.EMPTY_TANK);
            }
        }
        return FluidTanks.EMPTY_TANK;
    }).texture(GTGuiTextures.FLUID_SLOT).overlay(GTGuiTextures.FLUID_IN_SLOT_OVERLAY).build();
    //Cheat using same ID to get working counter.
    public static SlotType<ModularSlot> FL_OUT = SlotType.builder().id("fluid_out").fluidHandlerSupplier(g -> {
        if (g instanceof BlockEntityMachine<?> machine) {
            return machine.fluidHandler.map(FluidHandler::getOutputTanks).orElse(FluidTanks.EMPTY_TANK);
        }
        if (g instanceof ICover cover){
            if (cover.getFluidTanks() != null){
                return cover.getFluidTanks().getOrDefault(FluidTankType.OUTPUT, FluidTanks.EMPTY_TANK);
            }
        }
        return FluidTanks.EMPTY_TANK;
    }).texture(GTGuiTextures.FLUID_SLOT).overlay(GTGuiTextures.FLUID_OUT_SLOT_OVERLAY).build();
    public static SlotType<ModularSlot> FL_PHANTOM = SlotType.builder().id("fluid_phantom").fluidHandlerSupplier(g -> {
        if (g instanceof BlockEntityMachine<?> machine) {
            return machine.fluidHandler.map(FluidHandler::getPhantomTanks).orElse(FluidTanks.EMPTY_TANK);
        }
        if (g instanceof ICover cover){
            if (cover.getFluidTanks() != null){
                return cover.getFluidTanks().getOrDefault(FluidTankType.PHANTOM, FluidTanks.EMPTY_TANK);
            }
        }
        return FluidTanks.EMPTY_TANK;
    }).texture(GTGuiTextures.FLUID_SLOT).phantom(true).build();

    public static void init(){

    }
}
