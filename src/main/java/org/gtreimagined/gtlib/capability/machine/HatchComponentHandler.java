package org.gtreimagined.gtlib.capability.machine;

import org.gtreimagined.gtlib.blockentity.multi.BlockEntityHatch;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityMultiMachine;
import org.gtreimagined.gtlib.capability.ComponentHandler;
import org.jetbrains.annotations.NotNull;

public class HatchComponentHandler<T extends BlockEntityHatch<T>> extends ComponentHandler<T> {

    public HatchComponentHandler(T componentTile) {
        super(componentTile.getMachineType().getId(), componentTile.hatchMachine.getIdForHandlers(), componentTile);
    }

    @Override
    public void onStructureFormed(@NotNull BlockEntityMultiMachine<?> controllerTile) {
        super.onStructureFormed(controllerTile);
    }

    @Override
    public void onStructureInvalidated(@NotNull BlockEntityMultiMachine<?> controllerTile) {
        super.onStructureInvalidated(controllerTile);
    }

}
