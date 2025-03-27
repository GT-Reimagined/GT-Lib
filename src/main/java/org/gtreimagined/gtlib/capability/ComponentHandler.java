package org.gtreimagined.gtlib.capability;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.gtreimagined.gtlib.blockentity.BlockEntityBase;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityMultiMachine;
import org.gtreimagined.gtlib.capability.machine.MachineEnergyHandler;
import org.gtreimagined.gtlib.capability.machine.MachineFluidHandler;
import org.gtreimagined.gtlib.capability.machine.MachineItemHandler;
import org.gtreimagined.gtlib.structure.StructureCache;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class ComponentHandler<T extends BlockEntityBase<T>> implements IComponentHandler {

    protected String componentId;
    protected String idForHandlers;
    protected T componentTile;

    protected Set<BlockEntityMultiMachine<?>> controllers = new ObjectOpenHashSet<>();

    public ComponentHandler(String componentId, String idForHandlers, T componentTile) {
        this.componentId = componentId;
        this.idForHandlers = idForHandlers;
        this.componentTile = componentTile;
    }

    public ComponentHandler(String componentId, T componentTile) {
        this(componentId, componentId, componentTile);
    }

    @NotNull
    @Override
    public String getId() {
        return componentId;
    }

    @NotNull
    @Override
    public T getTile() {
        return componentTile;
    }

    @Override
    public @NotNull String getIdForHandlers() {
        return idForHandlers;
    }

    @NotNull
    @Override
    public Optional<MachineItemHandler<?>> getItemHandler() {
        return componentTile instanceof BlockEntityMachine<?> machine ? machine.itemHandler.map(h -> h) : Optional.empty();
    }

    @NotNull
    @Override
    public Optional<MachineFluidHandler<?>> getFluidHandler() {
        return componentTile instanceof BlockEntityMachine<?> machine ?  machine.fluidHandler.map(f -> f) : Optional.empty();
    }

    @NotNull
    @Override
    public Optional<MachineEnergyHandler<?>> getEnergyHandler() {
        return componentTile instanceof BlockEntityMachine<?> machine ? machine.energyHandler.map(e -> e) : Optional.empty();
    }

    @Override
    public void onStructureFormed(@NotNull BlockEntityMultiMachine<?> controllerTile) {
        this.controllers.add(controllerTile);
    }

    @Override
    public void onStructureInvalidated(@NotNull BlockEntityMultiMachine<?> controllerTile) {
        this.controllers.remove(controllerTile);
    }

    @Override
    public boolean hasLinkedController() {
        return StructureCache.has(getTile().getLevel(), getTile().getBlockPos());
    }

    @NotNull
    @Override
    public Collection<BlockEntityMultiMachine<?>> getControllers() {
        return controllers;
    }
}
