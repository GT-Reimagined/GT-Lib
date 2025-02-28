package muramasa.antimatter.capability.machine;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import muramasa.antimatter.Ref;
import muramasa.antimatter.blockentity.BlockEntityMachine;
import muramasa.antimatter.capability.Dispatch;
import muramasa.antimatter.capability.IMachineHandler;
import muramasa.antimatter.capability.rf.FEHandler;
import muramasa.antimatter.gui.SlotType;
import muramasa.antimatter.machine.event.IMachineEvent;
import muramasa.antimatter.machine.event.MachineEvent;
import muramasa.antimatter.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import tesseract.api.fe.IFENode;

import java.util.List;
import java.util.Optional;

public class MachineFEHandler<T extends BlockEntityMachine<T>> extends FEHandler implements IMachineHandler, Dispatch.Sided<IFENode> {
    protected final T tile;
    protected List<IEnergyStorage> cachedItems = new ObjectArrayList<>();

    protected int offsetInsert = 0;
    protected int offsetExtract = 0;
    public MachineFEHandler(T tile, int energy, int capacity, int maxIn, int maxOut) {
        super(energy, capacity, maxIn, maxOut);
        this.tile = tile;
    }

    public MachineFEHandler(T tile, int capacity, boolean isGenerator) {
        this(tile, 0, capacity, isGenerator ? 0 : (int) tile.getMachineTier().getVoltage(), isGenerator ? (int) tile.getMachineTier().getVoltage() : 0);
    }

    public void onUpdate(){
        for (Direction dir : Ref.DIRS) {
            if (canExtract(dir)) {
                BlockEntity tile = this.tile.getCachedBlockEntity(dir);
                if (tile == null) continue;
                Optional<IEnergyStorage> handle = tile.getCapability(CapabilityEnergy.ENERGY, dir.getOpposite()).resolve();
                handle.ifPresent(eh -> Utils.transferEnergy(this, eh));
            }
        }
    }

    @Override
    public void init() {
        this.cachedItems = tile.itemHandler.map(MachineItemHandler::getFEChargeableItems).map(ImmutableList::copyOf).orElse(ImmutableList.of());
    }

    @Override
    public int getMaxEnergyStored() {
        if (canChargeItem()) {
            return super.getMaxEnergyStored() + (cachedItems != null ? cachedItems.stream().mapToInt(IEnergyStorage::getMaxEnergyStored).sum() : 0);
        }
        return super.getMaxEnergyStored();
    }

    @Override
    public int receiveEnergy(int maxAmount, boolean simulate) {
        int j = 0;
        int inserted = super.receiveEnergy(maxAmount, simulate);
        for (int i = offsetInsert; j < cachedItems.size(); j++, i = (i == cachedItems.size() - 1 ? 0 : (i + 1))) {
            IEnergyStorage handler = cachedItems.get(i);
            if (!handler.canReceive()) continue;
            int insert = handler.receiveEnergy(maxAmount, simulate);
            if (insert > 0) {
                offsetInsert = (offsetInsert + 1) % cachedItems.size();
                inserted += insert;
            }
        }
        if (inserted > 0) {
            tile.onMachineEvent(MachineEvent.ENERGY_INPUTTED);
        }
        return inserted;
    }

    @Override
    public int extractEnergy(int maxAmount, boolean simulate) {
        int j = 0;
        int extracted = super.extractEnergy(maxAmount, simulate);
        for (int i = offsetInsert; j < cachedItems.size(); j++, i = (i == cachedItems.size() - 1 ? 0 : (i + 1))) {
            IEnergyStorage handler = cachedItems.get(i);
            if (!handler.canExtract()) continue;
            int extract = handler.extractEnergy(maxAmount, simulate);
            if (extract > 0) {
                offsetInsert = (offsetInsert + 1) % cachedItems.size();
                extracted += extract;
            }
        }
        if (extracted > 0) {
            tile.onMachineEvent(MachineEvent.ENERGY_INPUTTED);
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        if (canChargeItem()) {
            return super.getEnergyStored() + (cachedItems != null ? cachedItems.stream().mapToInt(IEnergyStorage::getEnergyStored).sum() : 0);
        }
        return super.getEnergyStored();
    }

    @Override
    public boolean canReceive(Direction direction) {
        return super.canReceive(direction) && (tile.getFacing() != direction || tile.getMachineType().allowsFrontIO());
    }

    public boolean canChargeItem() {
        return true;
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object... data) {
        if (event == SlotType.ENERGY) {
            tile.itemHandler.ifPresent(h -> {
                cachedItems = h.getFEChargeableItems();
                offsetInsert = 0;
                offsetExtract = 0;
            });
            //refreshNet();
        }
    }

    @Override
    public LazyOptional<? extends IFENode> forSide(Direction side) {
        return LazyOptional.of(() -> this);
    }

    @Override
    public LazyOptional<? extends IFENode> forNullSide() {
        return LazyOptional.of(() -> this);
    }

    public void onRemove() {
    }
}
