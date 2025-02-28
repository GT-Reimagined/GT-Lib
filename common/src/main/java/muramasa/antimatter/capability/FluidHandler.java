package muramasa.antimatter.capability;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import muramasa.antimatter.blockentity.BlockEntityBase;
import muramasa.antimatter.capability.fluid.FluidTanks;
import muramasa.antimatter.capability.fluid.IFluidNode;
import muramasa.antimatter.gui.SlotType;
import muramasa.antimatter.machine.event.IMachineEvent;
import muramasa.antimatter.util.RegistryUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tesseract.api.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

public abstract class FluidHandler<T extends BlockEntityBase & IMachineHandler> implements IMachineHandler, IFluidNode, Serializable {
    @Getter
    protected final T tile;
    protected final EnumMap<FluidDirection, FluidTanks> tanks = new EnumMap<>(FluidDirection.class);
    protected int capacity;

    /**
     * For GUI
     **/
    protected boolean dirty;

    public FluidHandler(T tile, int capacity, int inputCount, int outputCount) {
        this.tile = tile;
        this.capacity = capacity;
        if (inputCount > 0) {
            tanks.put(FluidDirection.INPUT, FluidTanks.create(tile, SlotType.FL_IN, b -> {
                for (int i = 0; i < inputCount; i++) {
                    b.tank(capacity);
                }
                return b;
            }));
        }
        if (outputCount > 0) {
            tanks.put(FluidDirection.OUTPUT, FluidTanks.create(tile, SlotType.FL_OUT, b -> {
                for (int i = 0; i < outputCount; i++) {
                    b.tank(capacity);
                }
                return b;
            }));
        }
    }

    public void onRemove() {

    }

    public void onReset() {

    }

    public void onUpdate() {

    }

    @Override
    public int getTanks() {
        return this.tanks.values().stream().mapToInt(FluidTanks::getTanks).sum();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return getTank(tank).getFluid();
    }

    protected FluidTank getTank(int tank) {
        FluidTanks tanks = getTanks(tank);
        if (tanks == null)
            return null;
        return tanks.getTank(offsetTank(tank));
    }

    protected FluidTanks getTanks(int tank) {
        FluidTanks input = getInputTanks();
        FluidTanks output = getOutputTanks();
        boolean hasInput = input != null;
        boolean hasOutput = output != null;
        if (hasInput && !hasOutput) {
            return input;
        } else if (!hasInput && hasOutput) {
            return output;
        } else if (!hasOutput && !hasOutput) {
            return null;
        }

        boolean isOutput = tank >= input.getTanks();

        if (!isOutput) {
            return input;
        } else {
            return output;
        }
    }

    public int offsetTank(int tank) {
        FluidTanks in = getInputTanks();
        if (in != null && tank >= getInputTanks().getTanks())
            return tank - in.getTanks();
        return tank;
    }

    @Override
    public int getTankCapacity(int tank) {
        return getTanks(tank).getTankCapacity(offsetTank(tank));
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return getTank(tank).isFluidValid(stack);
    }

    public FluidTanks getAllTanks() {
        ObjectArrayList<FluidTank> list = new ObjectArrayList<>();
        if (getInputTanks() != null)
            list.addAll(Arrays.asList(getInputTanks().getBackingTanks()));
        if (getOutputTanks() != null)
            list.addAll(Arrays.asList(getOutputTanks().getBackingTanks()));
        return new FluidTanks(list);
    }

    @Override
    public int fill(FluidStack fluid, FluidAction action) {
        FluidTanks input = getInputTanks();
        if (input != null && !empty(input)) {
            return getInputTanks().fill(fluid, action);
        }
        return 0;
    }

    protected boolean empty(FluidTanks tank) {
        return tank.getTanks() == 0;
    }

    public int fillOutput(FluidStack stack, FluidAction action) {
        if (getOutputTanks() != null) {
            return getOutputTanks().fill(stack, action);
        }
        return 0;
    }

    @Override
    public FluidStack drain(FluidStack fluid, FluidAction action) {
        if (getOutputTanks() != null) {
            return getOutputTanks().drain(fluid, action);
        }
        return FluidStack.EMPTY;
    }

    @NotNull
    @Override
    public FluidStack drain(int toExtract, FluidAction action) {
        if (getOutputTanks() != null){
            return getOutputTanks().drain(toExtract, action);
        }
        return FluidStack.EMPTY;
    }

    /**
     * Drains from the input tanks rather than output tanks. Useful for recipes.
     *
     * @param stack  stack to drain.
     * @param action execute/simulate
     * @return the drained stack
     */
    @NotNull
    @Override
    public FluidStack drainInput(FluidStack stack, FluidAction action) {
        if (getInputTanks() != null) {
            return getInputTanks().drain(stack, action);
        }
        return FluidStack.EMPTY;
    }

    public FluidStack drainInput(int maxDrain, FluidAction action) {
        if (getInputTanks() != null){
            return getInputTanks().drain(maxDrain, action);
        }
        return FluidStack.EMPTY;
    }

    public void setFluid(int slot, FluidStack fluid) {
        getTank(slot).setFluid(fluid);
    }

    protected boolean checkValidFluid(FluidStack fluid) {
        return true;
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object... data) {
    }

    /**
     * Helpers
     **/
    @NotNull
    public FluidStack[] getInputs() {
        FluidTanks tanks = getInputTanks();
        return tanks == null ? new FluidStack[0] : tanks.getFluids().toArray(FluidStack[]::new);
    }

    public FluidStack[] getOutputs() {
        FluidTanks tanks = getOutputTanks();
        return tanks == null ? new FluidStack[0] : tanks.getFluids().toArray(FluidStack[]::new);
    }

    public List<FluidStack> getFluids() {
        List<FluidStack> list = new ArrayList<>();
        list.addAll(Arrays.asList(getInputs()));
        list.addAll(Arrays.asList(getOutputs()));
        return list;
    }

    public boolean isEmpty() {
        return getAllTanks().isEmpty();
    }

    @Nullable
    public FluidTanks getInputTanks() {
        return this.tanks.get(FluidDirection.INPUT);
    }

    @Nullable
    public FluidTanks getOutputTanks() {
        return this.tanks.get(FluidDirection.OUTPUT);
    }

    @Override
    public boolean canOutput() {
        return getOutputTanks() != null;
    }

    @Override
    public boolean canInput() {
        return getInputTanks() != null;
    }

    @Override
    public boolean canInput(Direction direction) {
        return canInput();
    }

    @Override
    public boolean canInput(FluidStack fluid, Direction direction) {
        return true;
    }

    @Override
    public boolean canOutput(Direction direction) {
        return canOutput();
    }

    @Override
    public int getPriority(Direction direction) {
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (this.tanks.containsKey(FluidDirection.INPUT)) {
            builder.append("Inputs:\n");
            for (int i = 0; i < getInputTanks().getTanks(); i++) {
                FluidStack stack = getInputTanks().getFluidInTank(i);
                if (!stack.isEmpty()) {
                    builder.append(RegistryUtils.getIdFromFluid(stack.getFluid())).append(" - ").append(stack.getAmount());
                    if (i != getInputTanks().getTanks() - 1) {
                        builder.append("\n");
                    }
                }
            }
        }
        if (this.tanks.containsKey(FluidDirection.OUTPUT)) {
            builder.append("Outputs:\n");
            for (int i = 0; i < getOutputTanks().getTanks(); i++) {
                FluidStack stack = getOutputTanks().getFluidInTank(i);
                if (!stack.isEmpty()) {
                    builder.append(RegistryUtils.getIdFromFluid(stack.getFluid())).append(" - ").append(stack.getAmount());
                    if (i != getOutputTanks().getTanks() - 1) {
                        builder.append("\n");
                    }
                }
            }
        }
        return builder.toString();
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        tanks.forEach((k, v) -> {
            if (!nbt.contains(k.toString()))
                return;
            v.deserialize(nbt.getList(k.toString(), Tag.TAG_COMPOUND));
        });
    }

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        tanks.forEach((k, v) -> {
            nbt.put(k.name(), v.serialize());
        });
        return nbt;
    }

    public enum FluidDirection {
        INPUT,
        OUTPUT
    }
}
