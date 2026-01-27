package org.gtreimagined.gtlib.capability.machine;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Setter;
import net.minecraft.world.level.Level.ExplosionInteraction;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.Dispatch;
import org.gtreimagined.gtlib.capability.EnergyHandler;
import org.gtreimagined.gtlib.capability.IMachineHandler;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.machine.MachineFlag;
import org.gtreimagined.gtlib.machine.event.IMachineEvent;
import org.gtreimagined.gtlib.machine.event.MachineEvent;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import org.gtreimagined.tesseract.api.forge.TesseractCaps;
import org.gtreimagined.tesseract.api.eu.EUTransaction;
import org.gtreimagined.tesseract.api.eu.IEUCable;
import org.gtreimagined.tesseract.api.eu.IEUNode;
import org.gtreimagined.tesseract.api.eu.IEnergyHandler;
import org.gtreimagined.tesseract.api.eu.IEnergyHandlerItem;

import java.util.List;
import java.util.Optional;

public class MachineEnergyHandler<T extends BlockEntityMachine<T>> extends EnergyHandler implements IMachineHandler, Dispatch.Sided<IEnergyHandler> {

    protected final T tile;
    @Setter
    protected long capacty;

    protected List<Pair<ItemStack, IEnergyHandlerItem>> cachedItems = new ObjectArrayList<>();
    protected int offsetInsert = 0;
    protected int offsetExtract = 0;
    boolean exploded = false;

    public MachineEnergyHandler(T tile, long energy, long capacity, long voltageIn, long voltageOut, int amperageIn, int amperageOut) {
        super(energy, capacity, voltageIn, voltageOut, amperageIn, amperageOut);
        this.capacty = capacity;
        this.tile = tile;
    }

    public MachineEnergyHandler(T tile, boolean isGenerator) {
        this(tile, 1, isGenerator);
    }

    public MachineEnergyHandler(T tile, int amps, boolean isGenerator) {
        this(tile, 0L, tile.getMachineTier().getVoltage() * (isGenerator ? 40L : 66L), isGenerator ? 0 : tile.getMachineTier().getVoltage(), isGenerator ? tile.getMachineTier().getVoltage() : 0, isGenerator ? 0 : amps, isGenerator ? amps : 0);
    }

    @Override
    public void init() {
        this.cachedItems = tile.itemHandler.map(MachineItemHandler::getChargeableItems).map(ImmutableList::copyOf).orElse(ImmutableList.of());
    }

    public List<Pair<ItemStack, IEnergyHandlerItem>> getCachedEnergyItems() {
        return this.cachedItems;
    }

    public void onRemove() {

    }

    @Override
    protected boolean checkVoltage(long voltage) {
        if (voltage > this.getInputVoltage()) {
            if (GTLibConfig.MACHINES_EXPLODE.get()) {
                if (!exploded){
                    Utils.createExplosion(this.tile.getLevel(), tile.getBlockPos(), 4.0F, ExplosionInteraction.BLOCK);
                    tile.getLevel().playSound(null, tile.getBlockPos(), Ref.MACHINE_EXPLODE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    exploded = true;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public long getCapacity() {
        if (canChargeItem()) {
            return capacity + (cachedItems != null ? cachedItems.stream().map(Pair::right).mapToLong(IEnergyHandler::getCapacity).sum() : 0);
        }
        return capacity;
    }

    @Override
    public long insertEu(long voltage, boolean simulate) {
        if (voltage < 0) return 0;
        if (inputAmperageCheck()) return 0;
        int loss = canInput() && canOutput() ? 1 : 0;
        voltage -= loss;
        if (!this.tile.getMachineType().has(MachineFlag.PARTIAL_AMPS) && cachedItems.isEmpty() && this.getEnergy() + voltage > this.getCapacity()) return 0;
        if (!simulate && !checkVoltage(voltage + loss)) return voltage + loss;
        if (cachedItems.isEmpty()){
            long superInsert = super.insertEu(voltage, simulate);
            if (superInsert > 0 && !simulate){
                tile.onMachineEvent(MachineEvent.ENERGY_INPUTTED);
            }
            return superInsert + loss;
        }
        long internalInsert = insertInternal(voltage, simulate);
        return internalInsert > 0 ? internalInsert + loss : 0;
    }

    protected boolean inputAmperageCheck(){
        return getState().getAmpsReceived() >= getInputAmperage();
    }

    public long insertInternal(long energy, boolean simulate) {
        if (cachedItems.isEmpty()) return super.insertInternal(energy, simulate);
        long euPerBattery = energy / cachedItems.size();
        long leftover = energy % cachedItems.size();
        long euInserted = 0;
        for (int i = 0; i < cachedItems.size(); i++){
            IEnergyHandlerItem handler = cachedItems.get(i).right();
            long inserted = handler.insertEu(euPerBattery, simulate);
            if (inserted > 0){
                euInserted += inserted;
                if (!simulate){
                    cachedItems.get(i).left().setTag(handler.getContainer().getTag());
                }
            }
            if (euPerBattery - inserted > 0) leftover+= euPerBattery - inserted;
        }
        int unsuccessful = 0;
        int i = 0;
        while (leftover > 0){
            IEnergyHandlerItem handler = cachedItems.get(i).right();;
            if (handler.insertEu(1, simulate) > 0){
                if (!simulate) cachedItems.get(i).left().setTag(handler.getContainer().getTag());
                leftover--;
                euInserted++;
                unsuccessful = 0;
            } else {
                unsuccessful++;
            }
            i++;
            if (i == cachedItems.size()) i = 0;
            if (unsuccessful == cachedItems.size()) break;
        }
        if (leftover > 0){
            long insert = super.insertInternal(leftover, simulate);
            euInserted += insert;
        }
        if (euInserted > 0){
            tile.onMachineEvent(MachineEvent.ENERGY_INPUTTED);
        }
        if (leftover == 0){
            this.state.receive(simulate, 1);
        }
        return euInserted;
    }

    @Override
    public long extractEu(long voltage, boolean simulate) {
        if (cachedItems.isEmpty()){
            long superExtract = super.extractEu(voltage, simulate);
            if (superExtract > 0 && !simulate){
                tile.onMachineEvent(MachineEvent.ENERGY_DRAINED);
            }
            return superExtract;
        }
        long superExtract = super.extractEu(voltage, simulate);
        voltage-= superExtract;
        long toExtract = Math.min(voltage, getEnergy());
        long euPerBattery = toExtract / cachedItems.size();
        long leftover = toExtract % cachedItems.size();
        long euExtracted = superExtract;
        for (int i = 0; i < cachedItems.size(); i++){
            IEnergyHandlerItem handler = cachedItems.get(i).right();
            long extracted = handler.extractEu(euPerBattery, simulate);
            if (extracted > 0){
                euExtracted += extracted;
                if (!simulate){
                    cachedItems.get(i).left().setTag(handler.getContainer().getTag());
                }
            }
            if (euPerBattery - extracted > 0) leftover+= euPerBattery - extracted;
        }
        int unsuccessful = 0;
        int i = 0;
        while (leftover > 0){
            IEnergyHandlerItem handler = cachedItems.get(i).right();;
            if (handler.extractEu(1, simulate) > 0){
                if (!simulate) cachedItems.get(i).left().setTag(handler.getContainer().getTag());
                leftover--;
                euExtracted++;
                unsuccessful = 0;
            } else {
                unsuccessful++;
            }
            i++;
            if (i == cachedItems.size()) i = 0;
            if (unsuccessful == cachedItems.size()) break;
        }
        if (leftover > 0){
            long extract = super.extractEu(leftover, simulate);
            euExtracted += extract;
        }
        if (euExtracted > 0 && !simulate){
            tile.onMachineEvent(MachineEvent.ENERGY_DRAINED);
        }
        return euExtracted;
    }

    @Override
    public long getEnergy() {
        if (canChargeItem()) {
            return super.getEnergy() + (cachedItems != null ? cachedItems.stream().map(Pair::right).mapToLong(IEnergyHandler::getEnergy).sum() : 0);
        }
        return super.getEnergy();
    }

    protected long getBatteryEnergy(){
        return cachedItems != null ? cachedItems.stream().map(Pair::right).mapToLong(IEnergyHandler::getEnergy).sum() : 0;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        cachedItems.forEach(t -> t.right().getState().onTick());
        int ampsExtracted = 0;
        long ampsAvailable = this.availableAmpsOutput();
        outer:
        for (Direction dir : Ref.DIRS) {
            if (canOutput(dir)) {
                BlockEntity tile = this.tile.getCachedBlockEntity(dir);
                if (tile == null) continue;
                if (tile instanceof IEUCable && (!(tile instanceof IEUNode node) || !node.isActuallyNode())){
                    if (this.tile.getNetwork() == null) continue;
                    if (!this.tile.connects(dir)) continue;
                    for (long amp = 0; amp < ampsAvailable; amp++) {
                        long extracted = this.extractEu(this.getOutputVoltage(), true);
                        if (extracted > 0){
                            EUTransaction transaction = new EUTransaction(extracted, t -> {});
                            this.tile.getNetwork().insert(transaction, this.tile);
                            long insertEu = extracted - transaction.eu;
                            if (insertEu > 0){
                                this.extractEu(insertEu, false);
                                transaction.commit();
                                ampsExtracted++;
                                if (ampsExtracted == ampsAvailable) break outer;
                            }
                        }
                    }
                } else {
                    Optional<IEnergyHandler> handle = tile.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY, dir.getOpposite()).resolve();
                    if (handle.map(h -> !h.canInput(dir.getOpposite())).orElse(true)) continue;
                    handle.ifPresent(eh -> Utils.transferEnergy(this, eh));
                }

            }
        }
    }

    /*@Override
    public long availableAmpsOutput() {
        return super.availableAmpsOutput() + this.cachedItems.stream().map(Pair::right).mapToLong(IEUNode::availableAmpsOutput).sum();
    }*/

    /*@Override
    public long availableAmpsInput(long voltage) {
        return super.availableAmpsInput(voltage) + this.cachedItems.stream().map(Pair::right).mapToLong(node -> node.availableAmpsInput(voltage)).sum();
    }*/

    @Override
    public boolean canInput(Direction direction) {
        return super.canInput(direction) && (tile.getFacing() != direction || tile.getMachineType().allowsFrontIO());
    }

    public boolean canChargeItem() {
        return true;
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object... data) {
        if (event == SlotType.ENERGY) {
            tile.itemHandler.ifPresent(h -> {
                cachedItems = h.getChargeableItems();
                offsetInsert = 0;
                offsetExtract = 0;
            });
            //refreshNet();
        }
    }


    @Override
    public LazyOptional<IEnergyHandler> forSide(Direction side) {
        return LazyOptional.of(() -> this);
    }

    @Override
    public LazyOptional<? extends IEnergyHandler> forNullSide() {
        return LazyOptional.of(() -> this);
    }
}