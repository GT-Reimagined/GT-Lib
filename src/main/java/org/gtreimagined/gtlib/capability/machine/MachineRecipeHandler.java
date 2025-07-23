package org.gtreimagined.gtlib.capability.machine;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.Dispatch;
import org.gtreimagined.gtlib.capability.IMachineHandler;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.machine.MachineFlag;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.event.IMachineEvent;
import org.gtreimagined.gtlib.machine.event.MachineEvent;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.IRecipeValidator;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.util.FluidUtils;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.gtreimagined.gtlib.machine.MachineFlag.EU;
import static org.gtreimagined.gtlib.machine.MachineFlag.FE;
import static org.gtreimagined.gtlib.machine.MachineState.*;

//TODO: This needs some look into, a bit of spaghetti code sadly.
public class MachineRecipeHandler<T extends BlockEntityMachine<T>> implements IMachineHandler, Dispatch.Sided<MachineRecipeHandler<?>> {

    protected final T tile;
    @Getter
    protected final boolean generator;
    @Getter
    protected IRecipe lastRecipe = null;
    /**
     * Indices:
     * 1 -> Progress of recipe
     */

    @Getter
    @Nullable
    protected IRecipe activeRecipe;
    protected boolean consumedResources;
    @Getter
    protected int currentProgress,
            maxProgress;
    @Getter
    protected long totalPowerToGenerate, powerGenerated;

    @Getter
    @Setter
    protected boolean processingBlocked = false;
    protected int overclock;

    //20 seconds per check.
    static final int WAIT_TIME = 20 * 20;
    static final int WAIT_TIME_POWER_LOSS = 20 * 5;
    protected static final int WAIT_TIME_OUTPUT_FULL = 20;
    protected int tickTimer = 0;

    //Consuming resources can call into the recipe handler, causing a loop.
    //For instance, consuming fluid in the fluid handlers calls back into the MachineRecipeHandler, deadlocking.
    //So just 'lock' during recipe ticking.
    private boolean tickingRecipe = false;

    //Items used to find recipe
    protected List<ItemStack> itemInputs = Collections.emptyList();
    protected List<FluidStack> fluidInputs = Collections.emptyList();

    public MachineRecipeHandler(T tile) {
        this.tile = tile;
        this.generator = tile.getMachineType().has(MachineFlag.GENERATOR);
    }


    public void getInfo(List<String> builder) {
        if (activeRecipe != null) {
            if (tile.getMachineState() != ACTIVE) {
                builder.add("Active recipe but not running");
            }
            builder.add("Progress: " + currentProgress + "/" + maxProgress);
        } else {
            builder.add("No recipe active");
        }
    }

    public boolean hasRecipe() {
        return activeRecipe != null;
    }

    public IRecipeMap getRecipeMap() {
        return tile.getMachineType().getRecipeMap(tile.getMachineTier());
    }

    public float getClientProgress() {
        return ((float) currentProgress / (float) maxProgress);
    }

    @Override
    public void init() {
        checkRecipe();
    }

    public void resetProgress(){
        this.currentProgress = 0;
    }

    public void onServerUpdate() {
        if (tile.getMachineState() == INVALID_STRUCTURE || tile.getMachineState() == DISABLED) return;
        //First, a few timer related tasks that ensure the machine can recover from certain situations.
        if (tickingRecipe) return;
        if (tickTimer > 0) {
            tickTimer--;
            if (tickTimer > 0) {
                return;
            }
        }
        if (tile.getMachineState() == OUTPUT_FULL) {
            if (canOutput()) {
                tile.setMachineState(recipeFinish());
                return;
            }
        }
        tickingRecipe = true;
        MachineState state = tickRecipe();
        if (tile.getMachineState() != state){
            tile.setMachineState(state);
        }
        tickingRecipe = false;
    }

    protected void logString(String message){
    }

    public IRecipe findRecipe() {
        if (lastRecipe != null) {
            activeRecipe = lastRecipe;
            if (canRecipeContinue()) {
                activeRecipe = null;
                return lastRecipe;
            }
            activeRecipe = null;
        }
        IRecipeMap map = getRecipeMap();
        return map != null ? map.find(tile.itemHandler, tile.fluidHandler, tile.getMachineTier(), this::validateRecipe) : null;
    }

    protected IRecipe cachedRecipe() {
        if (lastRecipe != null) {
            if (!lastRecipe.isValid()) {
                lastRecipe = null;
                return null;
            }
            IRecipe old = activeRecipe;
            activeRecipe = lastRecipe;
            if (canRecipeContinue()) {
                activeRecipe = old;
                return lastRecipe;
            }
            activeRecipe = old;
        }
        return null;
    }

    public int getOverclock() {
        if (activeRecipe == null) return 0;
        int oc = 0;
        if (activeRecipe.getPower() > 0 && this.tile.getPowerLevel().getVoltage() > activeRecipe.getPower()) {
            long voltage = this.activeRecipe.getPower();
            int tier = Utils.getVoltageTier(voltage);
            long tempoverclock = (this.tile.getPowerLevel().getVoltage() / Ref.V[tier]);
            while (tempoverclock > 1) {
                tempoverclock >>= 2;
                oc++;
            }
        }
        return oc;
    }

    public long getPower() {
        if (activeRecipe == null) return 0;
        if (overclock == 0 || tile.has(MachineFlag.FE)) return activeRecipe.getPower();
        //half the duration => overclock ^ 2.
        //so if overclock is 2 tiers, we have 1/4 the duration(200 -> 50) but for e.g. 8eu/t this would be
        //8*4*4 = 128eu/t.
        return (activeRecipe.getPower() * (1L << overclock) * (tile.getMachineType().isNoOverclockCost() ? 1L : (1L << overclock)));
    }

    protected void calculateDurations() {
        maxProgress = activeRecipe.getDuration();
        if (generator){
            totalPowerToGenerate = (activeRecipe.getTotalPower() * getEfficiency()) / 100;
        }
        if (!generator && !tile.has(MachineFlag.FE)) {
            overclock = getOverclock();
            this.maxProgress = Math.max(1, maxProgress >> overclock);
        }
    }

    protected void activateRecipe(boolean reset) {
        //if (canOverclock)
        consumedResources = false;
        tickTimer = 0;
        if (reset) {
            currentProgress = 0;
            powerGenerated = 0;
        }
        lastRecipe = activeRecipe;
    }

    protected void addOutputs() {
        if (activeRecipe.hasOutputItems()) {
            tile.itemHandler.ifPresent(h -> {
                //Roll the chances here..
                ItemStack[] out = activeRecipe.getOutputItems(true);
                if (h.canOutputsFit(out)) {
                    h.addOutputs(out);
                }
                tile.onMachineEvent(MachineEvent.ITEMS_OUTPUTTED);
            });
        }
        if (activeRecipe.hasOutputFluids()) {
            tile.fluidHandler.ifPresent(h -> {
                h.addOutputs(activeRecipe.getOutputFluids());
                tile.onMachineEvent(MachineEvent.FLUIDS_OUTPUTTED);
            });
        }
    }

    protected MachineState recipeFinish() {
        if (activeRecipe == null){
            if (tile.getLevel().getGameTime() % 10 == 0){
                GTLib.LOGGER.info("recipe null when finishing recipe, should not be possible!");
                GTLib.LOGGER.info(tile.getMachineType().getLoc().toString());
            }
            return tile.getMachineState();
        }
        tickTimer = 0;
        addOutputs();
        this.itemInputs = new ObjectArrayList<>();
        this.fluidInputs = new ObjectArrayList<>();
        if (!canRecipeContinue()) {
            this.resetRecipe();
            checkRecipe();
            return activeRecipe != null ? ACTIVE : tile.getDefaultMachineState();
        } else {
            calculateDurations();
            activateRecipe(true);
            return ACTIVE;
        }
    }

    protected MachineState tickRecipe() {
        if (this.activeRecipe == null) {
            return tile.getDefaultMachineState();
        }
        if (generator) return tickGeneratorRecipe();
        if (this.currentProgress >= this.maxProgress) {
            if (!canOutput()) {
                tickTimer += WAIT_TIME_OUTPUT_FULL;
                return OUTPUT_FULL;
            }
            MachineState state = recipeFinish();
            if (state != ACTIVE) return state;
        }

        tile.onRecipePreTick();
        if (!consumePower(true)){
            consumePower(false);
            if (currentProgress == 0 && (tile.getMachineState() == tile.getDefaultMachineState() || tile.getMachineState() == NO_POWER)) return NO_POWER;
            tickTimer += WAIT_TIME_POWER_LOSS;
            recipeFailure();
            playInterruptSound();
            return POWER_LOSS;
        }
        if (consumedResources) this.consumePower(false);
        if (currentProgress == 0 && !consumedResources) {
            if (this.consumeInputs()){
                this.consumePower(false);
            }
        }

        this.currentProgress++;
        if (Machine.isAprilFools()){
            if (tile.getLevel().random.nextInt(10000) == 0){
                tile.getLevel().playSound(null, tile.getBlockPos(), Ref.JOHN_CENA, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            if (tile.getLevel().random.nextInt(10000) == 0){
                tile.getLevel().playSound(null, tile.getBlockPos(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0f, (1.0F + (tile.getLevel().random.nextFloat() - tile.getLevel().random.nextFloat()) * 0.2F) * 0.7F);
                tile.getLevel().playSound(null, tile.getBlockPos(), Ref.MACHINE_EXPLODE, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
        tile.onRecipePostTick();
        return ACTIVE;
    }

    protected MachineState tickGeneratorRecipe(){
        tile.onRecipePreTick();
        if (this.powerGenerated == totalPowerToGenerate) {
            if (!canOutput()) {
                tickTimer += WAIT_TIME_OUTPUT_FULL;
                return OUTPUT_FULL;
            }
            MachineState state = recipeFinish();
            if (state != ACTIVE) return state;
        }
        if (this.powerGenerated == 0 && !consumedResources) {
            if (!consumeInputs()) {
                tile.onRecipePostTick();
                return tile.getDefaultMachineState();
            }
        }
        if (!activeRecipe.hasInputFluids() || tile.has(MachineFlag.FE)){
            long generated = generatePower(true);
            if (generated > 0){
                this.generatePower(false);
                this.powerGenerated += generated;
            } else {
                tile.onRecipePostTick();
                return tile.getDefaultMachineState();
            }
        }
        return ACTIVE;
    }

    protected void playInterruptSound(){
        if (tile.getMachineState() == ACTIVE && !tile.isMuffled()) tile.getLevel().playSound(null, tile.getBlockPos(), Ref.INTERRUPT, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    protected boolean shouldConsumeResources() {
        return !generator;
    }

    protected void recipeFailure() {
        currentProgress = 0;
    }

    public boolean consumePower(boolean simulate){
        if (processingBlocked) return false;
        if (generator) return true;
        if (getPower() > 0){
            if (tile.energyHandler.isPresent() && tile.has(EU)){
                return tile.energyHandler.map(e -> e.extractEu(getPower(), simulate) >= getPower()).orElse(false);
            } else if (tile.feHandler.isPresent() && tile.has(FE)){
                return tile.feHandler.map(e -> e.extractEnergy((int) getPower(), simulate) >= getPower()).orElse(false);
            } else {
                return false;
            }
        }
        return true;
    }

    public long generatePower(boolean simulate){
        if (!generator) return 0;
        if (activeRecipe.getPower() <= 0) return 0;
        long generated = 0;
        if (tile.energyHandler.isPresent() && tile.has(EU)) generated = tile.energyHandler.map(e -> e.insertInternal(totalPowerToGenerate - powerGenerated, simulate)).orElse(0L);
        else if (tile.feHandler.isPresent() && tile.has(FE)) generated = tile.feHandler.map(e -> e.receiveEnergy((int)(totalPowerToGenerate - powerGenerated), simulate)).orElse(0);
        return generated;
    }

    protected boolean validateRecipe(IRecipe r) {
        long voltage = tile.getMachineType().getAmps() * tile.getMaxInputVoltage();
        boolean ok = this.generator || !tile.has(EU) || voltage >= r.getPower() / r.getAmps();
        List<ItemStack> consumed = this.tile.itemHandler.map(t -> t.consumeInputs(r, true)).orElse(Collections.emptyList());
        for (IRecipeValidator validator : r.getValidators()) {
            if (!validator.validate(r, tile)) {
                return false;
            }
        }
        return ok && (!consumed.isEmpty() || !r.hasInputItems() || consumedResources);
    }

    protected boolean hasLoadedInput() {
        return !itemInputs.isEmpty() || !fluidInputs.isEmpty();
    }

    public void checkRecipe() {
        if (activeRecipe != null) {
            return;
        }
        //First lookup.
        if (!this.tile.hadFirstTick() && hasLoadedInput()) {
            if (!tile.getMachineState().allowRecipeCheck()) return;
            activeRecipe = getRecipeMap().find(itemInputs.toArray(new ItemStack[0]), fluidInputs.toArray(new FluidStack[0]), this.tile.getMachineTier(), this::validateRecipe);
            if (activeRecipe == null) return;
            calculateDurations();
            lastRecipe = activeRecipe;
            return;
        }
        if (tile.getMachineState().allowRecipeCheck()) {
            if ((activeRecipe = cachedRecipe()) != null || (activeRecipe = findRecipe()) != null) {
                if (!validateRecipe(activeRecipe)) {
                    tile.setMachineState(INVALID_TIER);
                    activeRecipe = null;
                    return;
                }
                calculateDurations();
                if (!consumePower(true) || !canRecipeContinue()) {
                    activeRecipe = null;
                    tile.setMachineState(tile.getDefaultMachineState());
                    //wait half a second after trying again.
                    tickTimer += 10;
                    return;
                }
                activateRecipe(true);
                tile.setMachineState(ACTIVE);
            }
        }
    }

    public boolean accepts(ItemStack stack) {
        IRecipeMap map = getRecipeMap();
        return map == null || map.acceptsItem(stack);
    }

    public boolean accepts(FluidStack stack) {
        IRecipeMap map = getRecipeMap();
        return map == null || map.acceptsFluid(stack);
    }

    public boolean consumeInputs() {
        if (generator && tile.has(EU)) return consumeGeneratorInputs(false);
        boolean flag = true;
        if (!tile.hadFirstTick()) return true;
        if (activeRecipe.hasInputItems()) {
            flag &= tile.itemHandler.map(h -> {
                this.itemInputs = h.consumeInputs(activeRecipe, false);
                return !this.itemInputs.isEmpty();
            }).orElse(true);
        }
        if (activeRecipe.hasInputFluids()) {
            flag &= tile.fluidHandler.map(h -> {
                this.fluidInputs = h.consumeAndReturnInputs(activeRecipe.getInputFluids(), false);
                return !this.fluidInputs.isEmpty();
            }).orElse(true);
        }
        if (flag) consumedResources = true;
        return flag;
    }

    public boolean consumeGeneratorInputs(boolean simulate){
        if (activeRecipe == null) return false;
        if (activeRecipe.hasInputItems() || tile.has(FE)) {
            AtomicReference<List<ItemStack>> itemInputs = new AtomicReference<>(new ArrayList<>());
            boolean flag = true;
            if (activeRecipe.hasInputItems()) {
                flag &= tile.itemHandler.map(h -> {
                    itemInputs.set(h.consumeInputs(activeRecipe, simulate));
                    return !itemInputs.get().isEmpty();
                }).orElse(true);
            }
            if (activeRecipe.hasInputFluids() && tile.has(FE)){
                int toConsume = calculateGeneratorConsumption(activeRecipe);
                flag &= tile.fluidHandler.map(h -> {
                    FluidIngredient in = activeRecipe.getInputFluids().get(0);
                    int amount = in.drainedAmount(toConsume, h, true, true);
                    if (amount == toConsume) {
                        if (!simulate)
                            in.drain(amount, h, true, false);
                        return true;
                    }
                    return false;
                }).orElse(false);
            }
            if (flag) consumedResources = true;
            return flag;
        }
        if (!activeRecipe.hasInputFluids()) return false;
        int toConsume = consumedFluidPerOperation(activeRecipe);
        long toInsert = calculateGeneratorProduction(activeRecipe);
        MachineEnergyHandler<?> handler = tile.energyHandler.orElse(null);
        if (handler == null) return false;
        FluidStack mFluid = tile.fluidHandler.map(f -> f.getInputTanks().getTank(0).getFluid()).orElse(FluidStack.EMPTY);
        if (mFluid.isEmpty()) return false;
        int fluidAmount = mFluid.getAmount();
        if (toInsert > 0 && toConsume > 0 && fluidAmount >= toConsume) {
            int tFluidAmountToUse = (int) Math.min(fluidAmount / toConsume, (handler.getCapacity() - handler.getEnergy()) / toInsert);
            if (tFluidAmountToUse > 0 && handler.insertInternal(tFluidAmountToUse * toInsert, true) == tFluidAmountToUse * toInsert) {
                if (tile.getLevel().getGameTime() % 10 == 0 && !simulate){
                    handler.insertInternal(tFluidAmountToUse * toInsert, false);
                    tile.fluidHandler.ifPresent(f -> f.drainInput(Utils.ca(tFluidAmountToUse * toConsume, mFluid), FluidAction.EXECUTE));
                }
                return true;
            }
        }
        return false;
    }

    public boolean canOutput() {
        //ignore chance for canOutput.
        if (tile.itemHandler.isPresent() && activeRecipe.hasOutputItems() && !tile.itemHandler.map(t -> t.canOutputsFit(activeRecipe.getOutputItems(false))).orElse(false))
            return false;
        return !tile.fluidHandler.isPresent() || !activeRecipe.hasOutputFluids() || tile.fluidHandler.map(t -> t.canOutputsFit(activeRecipe.getOutputFluids())).orElse(false);
    }

    protected boolean canRecipeContinue() {
        return canOutput() && (!activeRecipe.hasInputItems() || tile.itemHandler.map(i -> i.consumeInputs(this.activeRecipe, true).size() > 0).orElse(false)) && (!activeRecipe.hasInputFluids() || tile.fluidHandler.map(t -> t.consumeAndReturnInputs(activeRecipe.getInputFluids(), true).size() > 0).orElse(false));
    }

    protected long calculateGeneratorProduction(IRecipe r){
        return ( r.getPower() * getEfficiency() * consumedFluidPerOperation(r)) / 100;
    }

    public int consumedFluidPerOperation(IRecipe r){
        return r.getInputFluids().get(0).getAmount();
    }

    protected int getEfficiency() {
        return tile.getMachineType().getMachineEfficiency(tile.getMachineTier());
    }

    protected int calculateGeneratorConsumption(IRecipe r) {
        return r.getInputFluids().get(0).getAmount();
    }

    public void resetRecipe() {
        this.activeRecipe = null;
        this.consumedResources = false;
        this.currentProgress = 0;
        this.overclock = 0;
        this.maxProgress = 0;
        powerGenerated = 0;
        totalPowerToGenerate = 0;
        this.itemInputs = Collections.emptyList();
        this.fluidInputs = Collections.emptyList();
    }

    public void onMultiBlockStateChange(boolean isValid, boolean hardcore) {
        if (isValid) {
            if (tile.hadFirstTick()) {

                if (!hasRecipe()) {
                    checkRecipe();
                }
            }
        } else {
            if (activeRecipe != null) tile.onMachineStop();
            if (hardcore) {
                resetRecipe();
            }
            tile.resetMachine();
        }
    }

    public void onRemove() {
        resetRecipe();
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object... data) {
        if (tickingRecipe) return;
        if (event instanceof SlotType<?>) {
            if (tile.getMachineState() == ACTIVE)
                return;
            if (tile.getMachineState() == POWER_LOSS) {
                return;
            }
            if (activeRecipe != null && !consumePower(true)) {
                return;
            }
            if (event == SlotType.ENERGY) {
                if (tile.itemHandler.map(t -> t.inventories.get(SlotType.ENERGY).getStackInSlot((int) data[0]).isEmpty()).orElse(true)) {
                    return;
                }
            }
            if ((event == SlotType.IT_OUT || event == SlotType.FL_OUT) && tile.getMachineState() == OUTPUT_FULL && tickTimer == 0 && canOutput()) {
                tickingRecipe = true;
                tile.setMachineState(recipeFinish());
                tickingRecipe = false;
                return;
            }
            if (tile.getMachineState().allowRecipeCheck()) {
                if (activeRecipe == null) {
                    if (tile.getMachineState() != POWER_LOSS && tickTimer == 0) {
                        checkRecipe();
                    } else if (event == SlotType.IT_IN || event == SlotType.FL_IN) {
                        checkRecipe();
                    }
                }
            }
        } else if (event instanceof MachineEvent) {
            switch ((MachineEvent) event) {
                case ENERGY_INPUTTED, HEAT_INPUTTED -> {
                    if (event == MachineEvent.HEAT_INPUTTED && !tile.has(MachineFlag.HEAT)) break;
                    if (activeRecipe != null) {
                        break;
                    }
                    if (tile.getMachineState().allowRecipeCheck() && tile.getMachineState() != POWER_LOSS && tickTimer == 0) {
                        checkRecipe();
                    }
                }
                case ENERGY_DRAINED, HEAT_DRAINED -> {
                    if (event == MachineEvent.HEAT_DRAINED && !tile.has(MachineFlag.HEAT)) break;
                    if (generator && tile.getMachineState() == tile.getDefaultMachineState()) {
                        if (activeRecipe == null) {
                            checkRecipe();
                        }
                    }
                }
            }
        }
    }

    /**
     * NBT STUFF
     **/

    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        ListTag item = new ListTag();
        if (itemInputs.size() > 0) {
            itemInputs.forEach(t -> {
                item.add(t.save(new CompoundTag()));
            });
        }
        ListTag fluid = new ListTag();
        if (fluidInputs.size() > 0) {
            fluidInputs.forEach(t -> fluid.add(t.writeToNBT(new CompoundTag())));
        }
        nbt.put("I", item);
        nbt.putInt("T", tickTimer);
        nbt.put("F", fluid);
        nbt.putInt("P", currentProgress);
        nbt.putLong("PG", powerGenerated);
        nbt.putBoolean("C", consumedResources);
        nbt.putBoolean("PB", processingBlocked);
        if (activeRecipe != null){
            nbt.putString("AR", activeRecipe.getId().toString());
        }
        if (lastRecipe != null){
            nbt.putString("LR", lastRecipe.getId().toString());
        }
        return nbt;
    }

    public void deserialize(CompoundTag nbt) {
        itemInputs = new ObjectArrayList<>();
        fluidInputs = new ObjectArrayList<>();
        nbt.getList("I", 10).forEach(t -> itemInputs.add(ItemStack.of((CompoundTag) t)));
        nbt.getList("F", 10).forEach(t -> fluidInputs.add(FluidUtils.fromTag((CompoundTag) t)));
        this.processingBlocked = nbt.getBoolean("PB");
        this.currentProgress = nbt.getInt("P");
        this.powerGenerated = nbt.getLong("PG");
        this.tickTimer = nbt.getInt("T");
        this.consumedResources = nbt.getBoolean("C");
        if (getRecipeMap() != null) {
            this.activeRecipe = nbt.contains("AR") ? getRecipeMap().findByID(new ResourceLocation(nbt.getString("AR"))) : null;
            this.lastRecipe = nbt.contains("LR") ? getRecipeMap().findByID(new ResourceLocation(nbt.getString("LR"))) : null;
        }
        if (this.activeRecipe != null) calculateDurations();
    }

    @Override
    public LazyOptional<MachineRecipeHandler<?>> forSide(Direction side) {
        return LazyOptional.of(() -> this);
    }

    @Override
    public LazyOptional<MachineRecipeHandler<?>> forNullSide() {
        return LazyOptional.of(() -> this);
    }
}
