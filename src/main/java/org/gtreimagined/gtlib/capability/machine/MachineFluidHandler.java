package org.gtreimagined.gtlib.capability.machine;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.Dispatch;
import org.gtreimagined.gtlib.capability.FluidHandler;
import org.gtreimagined.gtlib.capability.fluid.FluidHandlerNullSideWrapper;
import org.gtreimagined.gtlib.capability.fluid.FluidHandlerSidedWrapper;
import org.gtreimagined.gtlib.capability.fluid.FluidTanks;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.event.IMachineEvent;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;
import org.gtreimagined.gtlib.util.FluidUtils;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.gtreimagined.gtlib.machine.MachineFlag.GENERATOR;
import static org.gtreimagined.gtlib.machine.MachineFlag.GUI;

public class MachineFluidHandler<T extends BlockEntityMachine<T>> extends FluidHandler<T> implements Dispatch.Sided<IFluidHandler> {

    private boolean fillingCell = false;
    protected boolean filledLastTick = false;
    private int lastCellSlot = 0;

    public MachineFluidHandler(T tile, int capacity) {
        this(tile, capacity, tile.has(GUI) ? tile.getMachineType().getSlots(SlotType.FL_IN, tile.getMachineTier()).size() : 0,
                tile.has(GUI) ? tile.getMachineType().getSlots(SlotType.FL_OUT, tile.getMachineTier()).size() : 0, tile.has(GUI) ? tile.getMachineType().getSlots(SlotType.FL_PHANTOM, tile.getMachineTier()).size() : 0);
    }

    public MachineFluidHandler(T tile, int capacity, int inputCount, int outputCount) {
        this(tile, capacity, inputCount, outputCount, 0);
    }

    public MachineFluidHandler(T tile, int capacity, int inputCount, int outputCount, int phantomCount) {
        super(tile, capacity, inputCount, outputCount, phantomCount);
    }

    public MachineFluidHandler(T tile) {
        this(tile, 32000);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (filledLastTick) {
            tryFillCell(lastCellSlot, -1);
        }
    }

    public void fillCell(int cellSlot, int maxFill) {
        if (fillingCell) return;
        fillingCell = true;
        if (getInputTanks() != null) {
            filledLastTick = tile.itemHandler.map(ih -> {
                if (ih.getCellInputHandler() == null) {
                    return false;
                }
                ItemStack cell = ih.getCellInputHandler().getStackInSlot(cellSlot);
                if (cell.isEmpty()) {
                    return false;
                }
                boolean success = false;
                Predicate<ItemStack> predicate = s -> MachineItemHandler.insertIntoOutput(ih.getCellOutputHandler(), cellSlot, s, true).isEmpty();
                Consumer<ItemStack> consumer = s -> {
                    MachineItemHandler.insertIntoOutput(ih.getCellOutputHandler(), cellSlot, s, false);
                    MachineItemHandler.extractFromInput(ih.getCellInputHandler(), cellSlot, 1, false);
                };
                if (FluidUtils.fillItemFromContainer(maxFill, Utils.ca(1, cell), this.getCellAccessibleTanks(), predicate, consumer)){
                    success = true;
                    lastCellSlot = cellSlot;
                } else if (FluidUtils.emptyItemIntoContainer(maxFill, Utils.ca(1, cell), this.getCellAccessibleTanks(), predicate, consumer)){
                    success = true;
                    lastCellSlot = cellSlot;
                }
                return success;
            }).orElse(false);
        } else {
            filledLastTick = false;
        }
        fillingCell = false;
    }

    protected FluidTanks getCellAccessibleTanks(){
        return this.getAllTanks();
    }

    protected boolean checkValidFluid(FluidStack fluid) {
        if (tile.has(GENERATOR)) {
            IRecipe recipe = tile.getMachineType().getRecipeMap(tile.getMachineTier()).find(new ItemStack[0], new FluidStack[]{fluid}, Tier.ULV, r -> true);
            if (recipe != null) {
                return true;
            }
        }
        return true;
    }

    protected void tryFillCell(int slot, int maxFill) {
        if (tile.itemHandler.map(MachineItemHandler::getCellCount).orElse(0) > 0) {
            fillCell(slot, maxFill);
        }
    }

    @Override
    public int fill(FluidStack fluid, FluidAction action) {
        if (!tile.recipeHandler.map(t -> t.accepts(fluid)).orElse(true)) return 0;
        return super.fill(fluid, action);
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object... data) {
        super.onMachineEvent(event, data);
        if (event instanceof SlotType<?>) {
            if (event == SlotType.CELL_IN || event == SlotType.CELL_OUT) {
                if (data[0] instanceof Integer) tryFillCell((Integer) data[0], -1);
            } else if (event == SlotType.FL_IN || event == SlotType.FL_OUT) {
                if (data[0] instanceof Integer integer) tryFillCell(integer, -1);
                else {
                    for (int i = 0; i < tile.itemHandler.map(MachineItemHandler::getCellCount).orElse(0); i++) {
                        fillCell(i, -1);
                    }
                }
                if (this.tile.getMachineType().rendersContainedLiquids()) {
                    tile.sidedSync(true);
                }
            }
        }
    }

    public boolean canFluidBeAutoOutput(FluidStack fluid) {
        return true;
    }

    public boolean canOutputsFit(FluidStack[] outputs) {
        return getSpaceForOutputs(outputs) >= outputs.length;
    }

    public int getSpaceForOutputs(FluidStack[] outputs) {
        int matchCount = 0;
        if (getOutputTanks() != null) {
            for (FluidStack output : outputs) {
                int tank = getOutputTanks().getFirstAvailableTank(output, false);
                if (tank >= 0 && getOutputTanks().getTank(tank).fill(output, FluidAction.SIMULATE) == output.getAmount()) {
                    matchCount++;
                }
            }
        }
        return matchCount;
    }

    public void addOutputs(FluidStack... fluids) {
        if (getOutputTanks() == null) {
            return;
        }
        if (fluids != null) {
            for (FluidStack input : fluids) {
                fillOutput(input, FluidAction.EXECUTE);
            }
        }
    }

    public int getTankForTag(TagKey<Fluid> tag, int min) {
        FluidStack[] inputs = this.getInputs();
        for (int i = min; i < inputs.length; i++) {
            FluidStack input = inputs[i];
            if (input.getFluid().builtInRegistryHolder().is(tag)) {
                return i;
            }
        }
        return -1;
    }

    @NotNull
    public FluidStack consumeTaggedInput(TagKey<Fluid> input, int amount, boolean simulate) {
        FluidTanks inputs = getInputTanks();
        if (inputs == null) {
            return FluidStack.EMPTY;
        }
        int id = getTankForTag(input, 0);
        if (id == -1) return FluidStack.EMPTY;
        return inputs.drain(new FluidStack(inputs.getFluidInTank(id).getFluid(), amount), simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
    }

    @NotNull
    public List<FluidStack> consumeAndReturnInputs(List<FluidIngredient> inputs, boolean simulate) {
        if (getInputTanks() == null) {
            return Collections.emptyList();
        }
        List<FluidStack> consumed = new ObjectArrayList<>();
        List<FluidIngredient> fluidIngredients = new ObjectArrayList<>();
        if (inputs != null) {
            for (FluidIngredient input : inputs) {
                List<FluidStack> inner = input.drain(this, true, true);
                if (inner.stream().mapToLong(FluidStack::getAmount).sum() != input.getAmount()) {
                    return Collections.emptyList();
                } else {
                    fluidIngredients.add(input);
                    consumed.addAll(inner);
                }
            }
        }
        if (!simulate){
            fluidIngredients.forEach(f -> f.drain(this, true, false));
        }
        return consumed;
    }

    public FluidStack[] exportAndReturnOutputs(FluidStack... outputs) {
        if (getOutputTanks() == null) {
            return new FluidStack[0];
        }
        List<FluidStack> notExported = new ObjectArrayList<>();
        int result;
        for (int i = 0; i < outputs.length; i++) {
            result = fill(outputs[i], FluidAction.EXECUTE);
            if (result == 0) notExported.add(outputs[i]); //Valid space was not found
            else outputs[i] = Utils.ca(result, outputs[i]); //Fluid was partially exported
        }
        return notExported.toArray(new FluidStack[0]);
    }

    @Override
    public boolean canOutput(Direction direction) {
        if (tile.getFacing().get3DDataValue() == direction.get3DDataValue() && !tile.getMachineType().allowsFrontIO())
            return false;
        return super.canOutput();
    }

    @Override
    public boolean canInput(Direction direction) {
        if (tile.getFacing().get3DDataValue() == direction.get3DDataValue() && !tile.getMachineType().allowsFrontIO())
            return false;
        return super.canInput();
    }

    @Override
    public int getPriority(Direction direction) {
        return tile.coverHandler.map(c -> c.get(direction).getPriority(IFluidHandler.class)).orElse(0);
    }

    @Override
    public LazyOptional<? extends IFluidHandler> forNullSide() {
        return LazyOptional.of(() -> new FluidHandlerNullSideWrapper(this));
    }

    @Override
    public LazyOptional<IFluidHandler> forSide(Direction side) {
        return LazyOptional.of(() -> new FluidHandlerSidedWrapper(this, tile.coverHandler.map(c -> c).orElse(null), side));
    }

    public IFluidHandler getGuiHandler() {
        return new IFluidHandler() {
            @Override
            public int getTanks() {
                return MachineFluidHandler.this.getTanks();
            }

            @Override
            public @NotNull FluidStack getFluidInTank(int i) {
                return MachineFluidHandler.this.getFluidInTank(i);
            }

            @Override
            public int getTankCapacity(int i) {
                return MachineFluidHandler.this.getTankCapacity(i);
            }

            @Override
            public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
                return MachineFluidHandler.this.isFluidValid(i, fluidStack);
            }

            @Override
            public int fill(FluidStack fluidStack, FluidAction fluidAction) {
                return MachineFluidHandler.this.fill(fluidStack, fluidAction);
            }

            @Override
            public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
                return MachineFluidHandler.this.drain(fluidStack, fluidAction);
            }

            @Override
            public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
                return MachineFluidHandler.this.drain(i, fluidAction);
            }
        };
    }
}
