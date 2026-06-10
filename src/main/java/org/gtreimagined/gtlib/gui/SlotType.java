package org.gtreimagined.gtlib.gui;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.widgets.slot.ModularSlot;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.FluidHandler;
import org.gtreimagined.gtlib.capability.FluidHandler.FluidTankType;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.capability.fluid.FluidTanks;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.gui.slot.AbstractSlot;
import org.gtreimagined.gtlib.gui.slot.SlotCell;
import org.gtreimagined.gtlib.gui.slot.SlotEnergy;
import org.gtreimagined.gtlib.gui.slot.SlotFake;
import org.gtreimagined.gtlib.gui.slot.SlotInput;
import org.gtreimagined.gtlib.gui.slot.SlotOutput;
import org.gtreimagined.gtlib.machine.event.IMachineEvent;
import org.gtreimagined.gtlib.mui.GTGuiTextures;
import org.gtreimagined.gtlib.registration.IGTObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.gtreimagined.tesseract.api.forge.TesseractCaps;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

@Builder
public class SlotType<T extends ModularSlot> implements IGTObject, IMachineEvent {

    static final BiPredicate<IGuiHandler, ItemStack> NO_INPUT = (gu, stack) -> false;

    public static SlotType<SlotInput> IT_IN = SlotType.<SlotInput>builder().id("item_in")
            .slotSupplier((type, gui, inv, i, d) -> new SlotInput(type, gui, inv.getOrDefault(type, new EmptyHandler()), i))
            .tester(new ItIn()).output(false).build();
    public static SlotType<SlotOutput> IT_OUT = SlotType.<SlotOutput>builder().id("item_out")
            .slotSupplier((type, gui, inv, i, d) -> new SlotOutput(type, gui, inv.getOrDefault(type, new EmptyHandler()), i))
            .tester(NO_INPUT).slotGroup(false).input(false).build();

    public static SlotType<SlotFake> DISPLAY = SlotType.<SlotFake>builder().id("display")
            .slotSupplier((type, gui, item, i, d) -> new SlotFake(type, gui, item.getOrDefault(type, new EmptyHandler()), i, false))
            .tester(NO_INPUT).slotGroup(false).input(false).output(false).phantom(true).build();
    public static SlotType<SlotFake> DISPLAY_SETTABLE = SlotType.<SlotFake>builder().id("display_settable")
            .slotSupplier((type, gui, item, i, d) -> new SlotFake(type, gui, item.getOrDefault(type, new EmptyHandler()), i, true))
            .tester(NO_INPUT).slotGroup(false).output(false).phantom(true).build();
    public static SlotType<AbstractSlot<?>> STORAGE = SlotType.<AbstractSlot<?>>builder().id("storage").slotSupplier((type, gui, item, i, d) -> new AbstractSlot<>(type, gui, item.getOrDefault(type, new EmptyHandler()), i))
            .tester((t, i) -> true).build();
    public static SlotType<SlotCell> CELL_IN = SlotType.<SlotCell>builder().id("cell_in").slotSupplier((type, gui, inv, i, d) -> new SlotCell(type, gui, inv.getOrDefault(type, new EmptyHandler()), i))
            .tester((t, i) -> i.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent())
            .output(false).overlay(GTGuiTextures.CELL_IN_SLOT_OVERLAY).build();
    public static SlotType<SlotCell> CELL_OUT = SlotType.<SlotCell>builder().id("cell_out").slotSupplier((type, gui, inv, i, d) -> new SlotCell(type, gui, inv.getOrDefault(type, new EmptyHandler()), i))
            .tester((t, i) -> i.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent())
            .input(false).slotGroup(false).overlay(GTGuiTextures.CELL_OUT_SLOT_OVERLAY).build();
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
            }).output(false).overlay(GTGuiTextures.ENERGY_SLOT_OVERLAY).build();
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

    @Getter
    private String id;
    @Getter
    private ISlotSupplier<T> slotSupplier;
    @Getter
    private Function<IGuiHandler, FluidTanks> fluidHandlerSupplier;
    @Default
    @Getter
    private boolean output = true;
    @Default
    @Getter
    private boolean input = true;
    @Default
    @Getter
    private boolean phantom = false;
    @Default
    @Getter
    private boolean slotGroup = true;
    @Default
    @Getter
    private UITexture texture = GTGuiTextures.ITEM_SLOT;
    @Getter
    private UITexture overlay;
    @Getter
    @Default
    private BiPredicate<IGuiHandler, ItemStack> tester = (g, i) -> true;

    public interface ISlotSupplier<T extends ModularSlot> {
        T get(SlotType<T> type, IGuiHandler tile, Map<SlotType<?>, IItemHandler> slots, int index, SlotData<T> data);
    }

    public static void init() {

    }

    public static class ItIn implements BiPredicate<IGuiHandler, ItemStack> {

        @Override
        public boolean test(IGuiHandler iGuiHandler, ItemStack stack) {
            if (iGuiHandler instanceof BlockEntityMachine) {
                return (((BlockEntityMachine<?>) iGuiHandler).recipeHandler.map(rh -> rh.accepts(stack)).orElse(true));
            }
            return true;
        }
    }

    public static <T extends ModularSlot> SlotTypeBuilder<T> builder(){
        return new CustomSlotTypeBuilder<>();
    }

    public static class CustomSlotTypeBuilder<T extends ModularSlot> extends SlotTypeBuilder<T> {

        @Override
        public SlotType<T> build() {
            SlotType<T> build = super.build();
            if (build.id == null) {
                throw new IllegalArgumentException("Missing id for slot type");
            }
            if (build.slotSupplier == null && build.fluidHandlerSupplier == null) {
                throw new IllegalArgumentException("Slot Type must have either a fluid handler supplier or item slot supplier!");
            }
            GTAPI.register(SlotType.class, build);
            return build;
        }
    }

}
