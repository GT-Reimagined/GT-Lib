package org.gtreimagined.gtlib.capability.energy;

import org.gtreimagined.gtlib.GTLibConfig;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.gtreimagined.tesseract.api.context.TesseractItemContext;
import org.gtreimagined.tesseract.api.eu.EUState;
import org.gtreimagined.tesseract.api.eu.IEnergyHandlerItem;
import org.gtreimagined.tesseract.api.wrapper.ItemStackWrapper;

public class EnergyStackWrapper implements IEnergyHandlerItem {
    private final ItemStack stack;
    private final IEnergyStorage storage;

    private final EUState state = new EUState(this);

    public EnergyStackWrapper(ItemStack stack, IEnergyStorage storage) {
        this.stack = stack;
        this.storage = storage;
    }

    @Override
    public long insertEu(long voltage, boolean simulate) {
        return (long) (storage.receiveEnergy((int) (voltage * GTLibConfig.EU_TO_FE_RATIO.get()), simulate) / GTLibConfig.EU_TO_FE_RATIO.get());
    }

    @Override
    public long extractEu(long voltage, boolean simulate) {
        return (long) (storage.extractEnergy((int) (voltage * GTLibConfig.EU_TO_FE_RATIO.get()), simulate) / GTLibConfig.EU_TO_FE_RATIO.get());
    }

    @Override
    public long getEnergy() {
        return (long) (storage.getEnergyStored() / GTLibConfig.EU_TO_FE_RATIO.get());
    }

    @Override
    public long getCapacity() {
        return (long) (storage.getMaxEnergyStored() / GTLibConfig.EU_TO_FE_RATIO.get());
    }

    @Override
    public long getOutputAmperage() {
        return 1;
    }

    @Override
    public long getOutputVoltage() {
        return 32;
    }

    @Override
    public long getInputAmperage() {
        return 1;
    }

    @Override
    public long getInputVoltage() {
        return 32;
    }

    @Override
    public long availableAmpsInput(long voltage) {
        if (!canInput()) return 0;
        int inserted = storage.receiveEnergy((int) (voltage * GTLibConfig.EU_TO_FE_RATIO.get()), false);
        return inserted == voltage ? 1 : 0;
    }

    @Override
    public boolean canOutput() {
        return false;
        //return TesseractConfig.ENABLE_TRE_COMPAT.get() && storage.canExtract();
    }

    @Override
    public boolean canInput() {
        return storage.canReceive();
    }

    @Override
    public boolean canInput(Direction dir) {
        return canInput();
    }

    @Override
    public boolean canOutput(Direction direction) {
        return canOutput();
    }

    @Override
    public EUState getState() {
        return state;
    }

    @Override
    public void tesseractTick() {
        getState().onTick();
    }

    @Override
    public CompoundTag serialize(CompoundTag tag) {
        return null;
    }

    @Override
    public void deserialize(CompoundTag arg) {

    }

    @Override
    public void setCapacity(long capacity) {

    }

    @Override
    public void setEnergy(long energy) {

    }

    @Override
    public @NotNull TesseractItemContext getContainer() {
        return new ItemStackWrapper(this.stack);
    }
}