package muramasa.antimatter.capability.machine;

import muramasa.antimatter.blockentity.multi.BlockEntityMultiMachine;
import muramasa.antimatter.capability.IComponentHandler;
import muramasa.antimatter.capability.fluid.FluidTanks;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class MultiMachineFluidHandler<T extends BlockEntityMultiMachine<T>> extends MachineFluidHandler<T> {

    MachineFluidHandler<?>[] inputs = new MachineFluidHandler[0];
    MachineFluidHandler<?>[] outputs = new MachineFluidHandler[0];

    public MultiMachineFluidHandler(T tile) {
        super(tile);
        tanks.clear();
    }

    protected void cacheInputs() {
        inputs = tile.getComponentsByHandlerId(inputComponentString()).stream().map(IComponentHandler::getFluidHandler).map(Optional::get).sorted(this::compareInputHatches).toArray(MachineFluidHandler<?>[]::new);//this::allocateExtraSize);
        tanks.put(FluidDirection.INPUT, new FluidTanks(Arrays.stream(inputs).filter(t -> t.getInputTanks() != null).flatMap(t -> Arrays.stream(t.getInputTanks().getBackingTanks())).collect(Collectors.toList())));
    }

    protected int compareInputHatches(MachineFluidHandler<?> a, MachineFluidHandler<?> b) {
        return 0;
    }

    protected String inputComponentString(){
        return "fluid_input";
    }

    protected String outputComponentString(){
        return "fluid_output";
    }

    protected void cacheOutputs() {
        outputs = tile.getComponentsByHandlerId(outputComponentString()).stream().map(IComponentHandler::getFluidHandler).map(Optional::get).sorted(this::compareOutputHatches).toArray(MachineFluidHandler<?>[]::new);//this::allocateExtraSize);
        tanks.put(FluidDirection.OUTPUT, new FluidTanks(Arrays.stream(outputs).filter(t -> t.getOutputTanks() != null).flatMap(t -> Arrays.stream(t.getOutputTanks().getBackingTanks())).collect(Collectors.toList())));
    }

    protected int compareOutputHatches(MachineFluidHandler<?> a, MachineFluidHandler<?> b) {
        return 0;
    }

    public void invalidate() {
        tanks.clear();
        inputs = new MachineFluidHandler[0];
        outputs = new MachineFluidHandler[0];
    }

    public void onStructureBuild() {
        cacheInputs();
        cacheOutputs();
    }
}
