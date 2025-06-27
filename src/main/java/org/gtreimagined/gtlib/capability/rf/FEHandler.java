package org.gtreimagined.gtlib.capability.rf;

import lombok.Setter;
import org.gtreimagined.gtlib.Ref;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import tesseract.api.Serializable;
import tesseract.api.fe.IExtendedEnergyStorage;
import tesseract.api.fe.IFENode;


public class FEHandler implements IExtendedEnergyStorage, Serializable {

    protected final int capacity;

    @Setter
    protected int energy;
    protected int maxIn, maxOut;

    public FEHandler(int energy, int capacity, int maxIn, int maxOut) {
        this.energy = energy;
        this.capacity = capacity;
        this.maxIn = maxIn;
        this.maxOut = maxOut;
    }

    /**
     * Tesseract IEUNode Implementations
     **/

    public void setMaxOutput(int maxOutput) {
        this.maxOut = maxOutput;
    }

    public void setMaxInput(int maxInput) {
        this.maxIn = maxInput;
    }

    @Override
    public int extractEnergy(int maxAmount, boolean simulate) {
        int maxExtract = Math.min(maxAmount, this.energy);
        if (!simulate) this.energy -= maxExtract;
        return maxExtract;
    }

    @Override
    public int receiveEnergy(int maxAmount, boolean simulate) {
        int maxInsert = Mth.clamp(maxAmount, 0, capacity - energy);
        if (!simulate) this.energy += maxInsert;
        return maxInsert;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }
    @Override
    public int maxInsert() {
        return maxIn;
    }

    @Override
    public int maxExtract() {
        return maxOut;
    }

    @Override
    public boolean canReceive() {
        return maxIn > 0;
    }

    @Override
    public boolean canReceive(Direction direction) {
        return canReceive();
    }

    @Override
    public boolean canExtract(Direction direction) {
        return canExtract();
    }

    @Override
    public boolean canExtract() {
        return maxOut > 0;
    }

    @Override
    public CompoundTag serialize(CompoundTag tag) {
        tag.putLong(Ref.TAG_MACHINE_ENERGY, this.energy);
        tag.putLong(Ref.TAG_MACHINE_VOLTAGE_IN, this.maxIn);
        tag.putLong(Ref.TAG_MACHINE_VOLTAGE_OUT, this.maxOut);
        return tag;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        this.energy = nbt.getInt(Ref.TAG_MACHINE_ENERGY);
        this.maxIn = nbt.getInt(Ref.TAG_MACHINE_VOLTAGE_IN);
        this.maxOut = nbt.getInt(Ref.TAG_MACHINE_VOLTAGE_OUT);
    }
}
