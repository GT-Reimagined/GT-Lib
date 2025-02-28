package muramasa.antimatter.capability.machine;

import muramasa.antimatter.blockentity.multi.BlockEntityMultiMachine;
import muramasa.antimatter.capability.item.ITrackedHandler;
import muramasa.antimatter.capability.item.MultiTrackedItemHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class MultiMachineItemHandler<T extends BlockEntityMultiMachine<T>> extends MachineItemHandler<T> {

    protected Optional<ITrackedHandler> inputs = Optional.empty();
    protected Optional<ITrackedHandler> outputs = Optional.empty();

    public MultiMachineItemHandler(T tile) {
        super(tile);
    }

    @Override
    public boolean canOutputsFit(ItemStack[] a) {
        return outputs.isPresent() && a != null && super.canOutputsFit(a);
    }

    @Override
    public ITrackedHandler getInputHandler() {
        return inputs.orElseGet(this::calculateInputs);
    }

    public void invalidate() {
        inputs = Optional.empty();
        outputs = Optional.empty();
    }

    public void onStructureBuild() {
        inputs = Optional.of(calculateInputs());
        outputs = Optional.of(calculateOutputs());
    }

    protected ITrackedHandler calculateInputs() {
        List<IItemHandlerModifiable> handlers = tile.getComponentsByHandlerId(inputComponentString()).stream().filter(t -> t.getItemHandler().isPresent()).map(t -> t.getItemHandler().get()).sorted(this::compareInputBuses).map(MachineItemHandler::getInputHandler).collect(Collectors.toList());
        handlers.add(super.getInputHandler());
        return new MultiTrackedItemHandler(handlers.toArray(new IItemHandlerModifiable[0]));
    }

    protected int compareInputBuses(MachineItemHandler<?> a, MachineItemHandler<?> b) {
        return 0;
    }

    protected String inputComponentString(){
        return "item_input";
    }

    protected String outputComponentString(){
        return "item_output";
    }

    protected int compareOutputBuses(MachineItemHandler<?> a, MachineItemHandler<?> b) {
        return 0;
    }

    protected ITrackedHandler calculateOutputs() {
        List<IItemHandlerModifiable> handlers = tile.getComponentsByHandlerId(outputComponentString()).stream().filter(t -> t.getItemHandler().isPresent()).map(t -> t.getItemHandler().get()).sorted(this::compareOutputBuses).map(MachineItemHandler::getOutputHandler).collect(Collectors.toList());
        handlers.add(super.getOutputHandler());
        return new MultiTrackedItemHandler(handlers.toArray(new IItemHandlerModifiable[0]));
    }

    @Override
    public ITrackedHandler getOutputHandler() {
        return outputs.orElseGet(this::calculateOutputs);
    }
}
