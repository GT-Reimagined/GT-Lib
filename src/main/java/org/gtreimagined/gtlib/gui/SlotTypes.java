package org.gtreimagined.gtlib.gui;

import brachy.modularui.widgets.slot.ModularSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.FluidHandler;
import org.gtreimagined.gtlib.capability.FluidHandler.FluidTankType;
import org.gtreimagined.gtlib.capability.IGuiHandler;
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

import java.util.function.BiPredicate;

public class SlotTypes {
    static final BiPredicate<IGuiHandler, ItemStack> NO_INPUT = (gu, stack) -> false;
    static final BiPredicate<IGuiHandler, ItemStack> ITEM_IN_PRED = (g, stack) -> {
        if (g instanceof BlockEntityMachine<?> machine) {
            return machine.recipeHandler.map(rh -> rh.accepts(stack)).orElse(true);
        }
        return true;
    };
    public static SlotType<SlotInput> IT_IN = SlotType.create(b -> {
        b.setId("item_in");
        b.setSlotSupplier((type, gui, inv, i, d) -> new SlotInput(type, gui, inv.getOrDefault(type, EmptyHandler.INSTANCE), i));
        b.setTester(ITEM_IN_PRED);
        b.setAllowExternalOutput(false);
    });
    public static SlotType<SlotOutput> IT_OUT = SlotType.create(b -> {
        b.setId("item_out");
        b.setSlotSupplier((type, gui, inv, i, d) -> new SlotOutput(type, gui, inv.getOrDefault(type, EmptyHandler.INSTANCE), i));
        b.setTester(NO_INPUT);
        b.setSlotGroup(false);
        b.setAllowExternalInput(false);
        b.setMayPlace(false);
    });
    public static SlotType<SlotFake> DISPLAY = SlotType.create(b -> {
        b.setId("display");
        b.setSlotSupplier((type, gui, item, i, d) -> new SlotFake(type, gui, item.getOrDefault(type, EmptyHandler.INSTANCE), i, false));
        b.setTester(NO_INPUT);
        b.setSlotGroup(false);
        b.setAllowExternalInput(false);
        b.setAllowExternalOutput(false);
        b.setMayPlace(false);
        b.setMayPickup(false);
        b.setPhantom(true);
    });
    public static SlotType<SlotFake> DISPLAY_SETTABLE = SlotType.create(b -> {
        b.setId("display_settable");
        b.setSlotSupplier((type, gui, item, i, d) -> new SlotFake(type, gui, item.getOrDefault(type, EmptyHandler.INSTANCE), i, true));
        b.setTester(NO_INPUT);
        b.setSlotGroup(false);
        b.setAllowExternalInput(false);
        b.setAllowExternalOutput(false);
        b.setPhantom(true);
    });
    public static SlotType<AbstractSlot<?>> STORAGE = SlotType.create(b -> {
        b.setId("storage");
        b.setSlotSupplier((type, gui, item, i, d) -> new AbstractSlot<>(type, gui, item.getOrDefault(type, EmptyHandler.INSTANCE), i));
        b.setTester((t, i) -> true);
    });
    public static SlotType<SlotCell> CELL_IN = SlotType.create(b -> {
        b.setId("cell_in");
        b.setSlotSupplier((type, gui, inv, i, d) -> new SlotCell(type, gui, inv.getOrDefault(type, EmptyHandler.INSTANCE), i));
        b.setTester((t, i) -> i.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent());
        b.setAllowExternalOutput(false);
        b.setOverlay(GTGuiTextures.CELL_IN_SLOT_OVERLAY);
    });
    public static SlotType<SlotCell> CELL_OUT = SlotType.create(b -> {
        b.setId("cell_out");
        b.setSlotSupplier((type, gui, inv, i, d) -> new SlotCell(type, gui, inv.getOrDefault(type, EmptyHandler.INSTANCE), i));
        b.setTester((t, i) -> i.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent());
        b.setAllowExternalInput(false);
        b.setMayPlace(false);
        b.setSlotGroup(false);
        b.setOverlay(GTGuiTextures.CELL_OUT_SLOT_OVERLAY);
    });
    public static SlotType<SlotEnergy> ENERGY = SlotType.create(b -> {
        b.setId("energy");
        b.setSlotSupplier((type, gui, inv, i, d) -> new SlotEnergy(type, gui, inv.getOrDefault(type, EmptyHandler.INSTANCE), i));
        b.setTester((t, i) -> {
                    if (t instanceof BlockEntityMachine<?> machine) {
                        return machine.energyHandler.map(eh -> {
                            return i.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).map(inner -> {
                                return ((inner.getInputVoltage() | inner.getOutputVoltage()) == (eh.getInputVoltage() | eh.getOutputVoltage()));
                            }).orElse(i.getCapability(ForgeCapabilities.ENERGY).isPresent());
                        }).orElse(false);
                    }
                    return true;
                });
        b.setAllowExternalOutput(false);
        b.setOverlay(GTGuiTextures.ENERGY_SLOT_OVERLAY);
    });
    public static SlotType<ModularSlot> FL_IN = SlotType.create(b -> {
        b.setId("fluid_in");
        b.setFluidHandlerSupplier(g -> {
            if (g instanceof BlockEntityMachine<?> machine) {
                return machine.fluidHandler.map(FluidHandler::getInputTanks).orElse(FluidTanks.EMPTY_TANK);
            }
            if (g instanceof ICover cover) {
                if (cover.getFluidTanks() != null) {
                    return cover.getFluidTanks().getOrDefault(FluidTankType.INPUT, FluidTanks.EMPTY_TANK);
                }
            }
            return FluidTanks.EMPTY_TANK;
        });
        b.setTexture(GTGuiTextures.FLUID_SLOT);
        b.setOverlay(GTGuiTextures.FLUID_IN_SLOT_OVERLAY);
    });
    //Cheat using same ID to get working counter.
    public static SlotType<ModularSlot> FL_OUT = SlotType.create(b -> {
        b.setId("fluid_out");
        b.setFluidHandlerSupplier(g -> {
            if (g instanceof BlockEntityMachine<?> machine) {
                return machine.fluidHandler.map(FluidHandler::getOutputTanks).orElse(FluidTanks.EMPTY_TANK);
            }
            if (g instanceof ICover cover) {
                if (cover.getFluidTanks() != null) {
                    return cover.getFluidTanks().getOrDefault(FluidTankType.OUTPUT, FluidTanks.EMPTY_TANK);
                }
            }
            return FluidTanks.EMPTY_TANK;
        });
        b.setTexture(GTGuiTextures.FLUID_SLOT);
        b.setOverlay(GTGuiTextures.FLUID_OUT_SLOT_OVERLAY);
    });
    public static SlotType<ModularSlot> FL_PHANTOM = SlotType.create(b -> {
        b.setId("fluid_phantom");
        b.setFluidHandlerSupplier(g -> {
            if (g instanceof BlockEntityMachine<?> machine) {
                return machine.fluidHandler.map(FluidHandler::getPhantomTanks).orElse(FluidTanks.EMPTY_TANK);
            }
            if (g instanceof ICover cover) {
                if (cover.getFluidTanks() != null) {
                    return cover.getFluidTanks().getOrDefault(FluidTankType.PHANTOM, FluidTanks.EMPTY_TANK);
                }
            }
            return FluidTanks.EMPTY_TANK;
        });
        b.setTexture(GTGuiTextures.FLUID_SLOT);
        b.setPhantom(true);
    });

    public static void init(){

    }
}
