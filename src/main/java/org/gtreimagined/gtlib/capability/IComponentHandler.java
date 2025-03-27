package org.gtreimagined.gtlib.capability;

import org.gtreimagined.gtlib.blockentity.multi.BlockEntityMultiMachine;
import org.gtreimagined.gtlib.capability.machine.MachineEnergyHandler;
import org.gtreimagined.gtlib.capability.machine.MachineFluidHandler;
import org.gtreimagined.gtlib.capability.machine.MachineItemHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import tesseract.TesseractCapUtils;
import tesseract.api.heat.IHeatHandler;

import java.util.Collection;
import java.util.Optional;

//@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IComponentHandler {

    String getId();

    BlockEntity getTile();

    String getIdForHandlers();

    Optional<MachineItemHandler<?>> getItemHandler();

    Optional<MachineFluidHandler<?>> getFluidHandler();

    Optional<MachineEnergyHandler<?>> getEnergyHandler();

    default Optional<IHeatHandler> getHeatHandler() {
        return TesseractCapUtils.INSTANCE.getHeatHandler(getTile(), null);
    }

    void onStructureFormed(BlockEntityMultiMachine<?> tile);

    void onStructureInvalidated(BlockEntityMultiMachine<?> tile);

    boolean hasLinkedController();

    Collection<BlockEntityMultiMachine<?>> getControllers();
}
