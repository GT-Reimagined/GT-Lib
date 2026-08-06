package org.gtreimagined.gtlib.capability.machine;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import lombok.Getter;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.Dispatch;
import org.gtreimagined.gtlib.capability.IMachineHandler;
import org.gtreimagined.gtlib.capability.energy.EnergyStackWrapper;
import org.gtreimagined.gtlib.capability.item.FakeTrackedItemHandler;
import org.gtreimagined.gtlib.capability.item.ITrackedHandler;
import org.gtreimagined.gtlib.capability.item.ROCombinedInvWrapper;
import org.gtreimagined.gtlib.capability.item.SidedCombinedInvWrapper;
import org.gtreimagined.gtlib.capability.item.TrackedItemHandler;
import org.gtreimagined.gtlib.gui.SlotData;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.gtreimagined.tesseract.api.Serializable;
import org.gtreimagined.tesseract.api.forge.TesseractCaps;
import org.gtreimagined.tesseract.api.eu.IEnergyHandlerItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.gtreimagined.gtlib.machine.MachineFlag.GUI;

public class MachineItemHandler<T extends BlockEntityMachine<T>> implements IMachineHandler, Serializable, Dispatch.Sided<IItemHandler> {

    @Getter
    protected final T tile;
    protected final Object2ObjectMap<SlotType<?>, TrackedItemHandler<T>> inventories = new Object2ObjectOpenHashMap<>();

    public MachineItemHandler(T tile) {
        this.tile = tile;
        if (tile.has(GUI)) {
            Map<SlotType<?>, List<SlotData<?>>> map = tile.getMachineType().getSlots(tile.getMachineTier()).stream().collect(Collectors.groupingBy(SlotData::getType));
            for (var entry : map.entrySet()) {
                SlotType<?> type = entry.getKey();
                if (type.getSlotSupplier() != null) {
                    inventories.put(type, this.createTrackedHandler(type, tile));
                }

            }
        }
        inventories.defaultReturnValue(new TrackedItemHandler<>(tile, SlotType.STORAGE, 0, false, false, (a, b) -> false));
    }

    protected TrackedItemHandler<T> createTrackedHandler(SlotType<?> type, T tile){
        int count = tile.getMachineType().getCount(tile.getMachineTier(), type);
        if (type.isPhantom()) {
            return new FakeTrackedItemHandler<>(tile, type, count, type.allowExternalOutput(), type.allowExternalInput(), type.getTester());
        } else {
            return new TrackedItemHandler<>(tile, type, count, type.allowExternalOutput(), type.allowExternalInput(), type.getTester());
        }
    }

    public Map<SlotType<?>, IItemHandler> getAll() {
        return (Map<SlotType<?>, IItemHandler>) (Object) inventories;
    }


    public boolean allowsInput(Direction side){
        return true;
    }

    public boolean allowsOutput(Direction side){
        return true;
    }
    @Override
    public void init() {
        ///registerNet();
    }

    @Override
    public CompoundTag serialize(CompoundTag nbt) {
        this.inventories.forEach((f, i) -> nbt.put(f.getId(), i.serializeNBT()));
        return nbt;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        this.inventories.forEach((f, i) -> {
            if (!nbt.contains(f.getId())) return;
            i.deserializeNBT(nbt.getCompound(f.getId()));
        });
    }

    public void onUpdate() {

    }

    public boolean canItemBeAutoOutput(ItemStack item) {
        return true;
    }

    public List<ItemStack> getAllItems() {
        return inventories.values().stream().filter(t -> !(t instanceof FakeTrackedItemHandler)).flatMap(t -> {
            List<ItemStack> stacks = new ObjectArrayList<>(t.getSlots());
            for (int i = 0; i < t.getSlots(); i++) {
                stacks.add(t.getStackInSlot(i).copy());
            }
            return stacks.stream();
        }).collect(Collectors.toList());
    }

    public void onRemove() {

    }

    public static ItemStack insertIntoOutput(IItemHandler handler, int slot, @NotNull ItemStack stack, boolean simulate) {
        if (handler instanceof ITrackedHandler trackedHandler) {
            return trackedHandler.insertOutputItem(slot, stack, simulate);
        }
        return handler.insertItem(slot, stack, simulate);
    }

    public static ItemStack extractFromInput(IItemHandler handler, int slot, int amount, boolean simulate) {
        if (handler instanceof ITrackedHandler trackedHandler) {
            return trackedHandler.extractFromInput(slot, amount, simulate);
        }
        return handler.extractItem(slot, amount, simulate);
    }

    /**
     * Handler Access
     **/
    public ITrackedHandler getInputHandler() {
        return inventories.get(SlotType.IT_IN);
    }

    public ITrackedHandler getOutputHandler() {
        return inventories.get(SlotType.IT_OUT);
    }

    public ITrackedHandler getCellInputHandler() {
        return inventories.get(SlotType.CELL_IN);
    }

    public ITrackedHandler getCellOutputHandler() {
        return inventories.get(SlotType.CELL_OUT);
    }

    public ITrackedHandler getChargeHandler() {
        return inventories.get(SlotType.ENERGY);
    }

    public ITrackedHandler getHandler(SlotType<?> type) {
        return inventories.get(type);
    }

    public int getInputCount() {
        return getInputHandler().getSlots();
    }

    public int getOutputCount() {
        return getOutputHandler().getSlots();
    }

    public int getCellCount() {
        return getCellInputHandler().getSlots();
    }

    @NotNull
    public ItemStack[] getInputs() {
        return getInputList().toArray(new ItemStack[0]);
    }

    public ItemStack[] getOutputs() {
        return getOutputList().toArray(new ItemStack[0]);
    }

    public ItemStack getCellInput() {
        return getCellInputHandler().getStackInSlot(0);
    }

    public ItemStack getCellOutput() {
        return getCellInputHandler().getStackInSlot(1);
    }

    /**
     * Gets a list of non empty input Items
     **/
    public List<ItemStack> getInputList() {
        List<ItemStack> list = new ObjectArrayList<>();
        IItemHandler inputs = getInputHandler();
        for (int i = 0; i < inputs.getSlots(); i++) {
            if (!inputs.getStackInSlot(i).isEmpty()) {
                list.add(inputs.getStackInSlot(i).copy());
            }
        }
        return list;
    }

    /**
     * Returns a non-copied list of chargeable items.
     **/
    public List<Pair<ItemStack, IEnergyHandlerItem>> getChargeableItems() {
        List<Pair<ItemStack, IEnergyHandlerItem>> list = new ObjectArrayList<>();
        if (tile.isServerSide()) {
            IItemHandler chargeables = getChargeHandler();
            for (int i = 0; i < chargeables.getSlots(); i++) {
                ItemStack item = chargeables.getStackInSlot(i);
                if (!item.isEmpty()) {
                    getWrappedEnergyHandlerItem(item).ifPresent(e -> list.add(new ObjectObjectImmutablePair<>(item, e)));
                }
            }
        }
        return list;
    }

    public Optional<IEnergyHandlerItem> getWrappedEnergyHandlerItem(ItemStack stack){
        IEnergyHandlerItem energyHandler = stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).map(e -> e).orElse(null);
        if (energyHandler == null){
            IEnergyStorage storage = stack.getCapability(ForgeCapabilities.ENERGY).map(e -> e).orElse(null);
            if (storage instanceof IEnergyHandlerItem e){
                energyHandler = e;
            } else if (storage != null){
                energyHandler = new EnergyStackWrapper(stack, storage);
            }
        }
        return Optional.ofNullable(energyHandler);
    }

    public List<IEnergyStorage> getFEChargeableItems() {
        List<IEnergyStorage> list = new ObjectArrayList<>();
        if (tile.isServerSide()) {
            IItemHandler chargeables = getChargeHandler();
            for (int i = 0; i < chargeables.getSlots(); i++) {
                ItemStack item = chargeables.getStackInSlot(i);
                var cap = item.getCapability(ForgeCapabilities.ENERGY);
                if (!item.isEmpty() && cap.isPresent()) {
                    list.add(cap.resolve().get());
                }
            }
        }
        return list;
    }

    /**
     * Gets a list of non empty output Items
     **/
    public List<ItemStack> getOutputList() {
        List<ItemStack> list = new ObjectArrayList<>();
        IItemHandler outputs = getOutputHandler();
        for (int i = 0; i < outputs.getSlots(); i++) {
            ItemStack slot = outputs.getStackInSlot(i);
            if (!slot.isEmpty()) {
                list.add(slot.copy());
            }
        }
        return list;
    }


    public List<ItemStack> consumeInputs(List<Ingredient> items, boolean simulate) {
        if (items == null) return Collections.emptyList();
        IntSet skipSlots = new IntOpenHashSet(getInputHandler().getSlots());
        List<ItemStack> consumedItems = new ObjectArrayList<>();

        boolean success = items.stream().mapToInt(input -> {
            int failed = 0;
            ITrackedHandler wrap = getInputHandler();
            int countToReach = RecipeIngredient.count(input);
            for (int i = 0; i < wrap.getSlots(); i++) {
                ItemStack item = wrap.getStackInSlot(i);
                if (input.test(item) && !skipSlots.contains(i)) {
                    int toConsume = Math.min(item.getCount(), Math.max(countToReach - item.getCount(), countToReach));
                    countToReach -= toConsume;
                    skipSlots.add(i);
                    ItemStack copy = item.copy();
                    copy.setCount(toConsume);
                    consumedItems.add(copy);
                    if (!RecipeIngredient.ignoreConsume(input) && !simulate) wrap.extractFromInput(i, toConsume, simulate);
                    if (countToReach == 0) {
                        break;
                    }
                }
                if (i == wrap.getSlots() - 1) {
                    failed++;
                }
            }
            return failed;
        }).sum() == 0;
        //onSlotChanged should call dirty though, not sure if needed.
        if (!simulate && success) tile.setChanged();
        if (simulate) return success ? consumedItems : Collections.emptyList();
        return consumedItems;
    }

    /**
     * Consumes the inputs from the active recipe.
     *
     * @param recipe   active recipe.
     * @param simulate whether to execute or just return items.
     * @return a list of consumed items, or an empty list if it failed during simulation.
     */
    public List<ItemStack> consumeInputs(IRecipe recipe, boolean simulate) {
        if (!simulate && recipe.hasInputChances()){
            int[] chances = recipe.getInputChances();
            List<ItemStack> consumed = new ArrayList<>();
            for (int i = 0; i < chances.length; i++) {
                if (Ref.RNG.nextInt(10000) < chances[i]){
                    consumed.addAll(consumeInputs(Collections.singletonList(recipe.getInputItems().get(i)), false));
                }
            }
            if (!recipe.getInputItems().isEmpty() && consumed.isEmpty()){
                consumed.add(Data.DEBUG_SCANNER.get(1)); //so the consumeInputs returns true
            }
            return consumed;
        }
        return consumeInputs(recipe.getInputItems(), simulate);
    }

    /**
     * Fill the output slots with @outputs items.
     *
     * @param outputs the outputs to add.
     */
    public void addOutputs(ItemStack... outputs) {
        IItemHandler outputHandler = getOutputHandler();
        if (outputHandler == null || outputs == null || outputs.length == 0) {
            return;
        }
        addOutputs(Arrays.asList(outputs));
    }

    /**
     * Fill the output slots with @outputs items.
     *
     * @param outputs the outputs to add.
     */
    public void addOutputs(List<ItemStack> outputs) {
        IItemHandler outputHandler = getOutputHandler();
        if (outputHandler == null || outputs.isEmpty()) {
            return;
        }
        for (ItemStack output : outputs) {
            for (int i = 0; i < outputHandler.getSlots(); i++) {
                output = insertIntoOutput(outputHandler, i, output.copy(), false);
                if (output.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
     * Helpers
     **/
    public boolean canOutputsFit(ItemStack[] a) {
        if (a == null) return true;
        return canOutputsFit(Arrays.asList(a));
    }


    public boolean canOutputsFit(List<ItemStack> a) {
        if (a.isEmpty()) return true;
        IItemHandler outputHandler = getOutputHandler();
        boolean[] results = new boolean[a.size()];
        List<Integer> slotsTaken = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            for (int j = 0; j < outputHandler.getSlots(); j++) {
                if (slotsTaken.contains(j)) continue;
                results[i] |= insertIntoOutput(outputHandler, j, a.get(i), true).isEmpty();
                if (results[i]){
                    slotsTaken.add(j);
                    break;
                }
            }
        }
        for (boolean value : results) {
            if (!value) {
                return false;
            }
        }
        return true;
    }

    public int getSpaceForOutputs(ItemStack[] a) {
        int matchCount = 0;
        //Here, cast to use stack limit
        IItemHandler handler = getOutputHandler();
        if (!(handler instanceof TrackedItemHandler)) {
            return 0;
        }
        for (ItemStack stack : a) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack item = handler.getStackInSlot(i);
                if (item.isEmpty() || (Utils.equals(stack, item) && item.getCount() + stack.getCount() <= handler.getSlotLimit(i))) {
                    matchCount++;
                    break;
                }
            }
        }

        return matchCount;
    }

    public ItemStack[] consumeAndReturnInputs(ItemStack... inputs) {
        List<ItemStack> notConsumed = new ObjectArrayList<>();
        IItemHandler inputHandler = getInputHandler();
        for (ItemStack input : inputs) {
            for (int i = 0; i < inputHandler.getSlots(); i++) {
                if (Utils.equals(input, inputHandler.getStackInSlot(i))) {
                    ItemStack result = extractFromInput(inputHandler, i, input.getCount(), false);
                    if (!result.isEmpty()) {
                        if (result.getCount() == input.getCount()) {
                            break;
                        } else {
                            notConsumed.add(Utils.ca(input.getCount() - result.getCount(), input));
                        }
                    }
                } else if (i == inputHandler.getSlots() - 1) {
                    notConsumed.add(input);
                }
            }
        }
        return notConsumed.toArray(new ItemStack[0]);
    }

    public ItemStack[] exportAndReturnOutputs(ItemStack... outputs) {
        List<ItemStack> notExported = new ObjectArrayList<>();
        IItemHandler outputHandler = getOutputHandler();
        for (int i = 0; i < outputs.length; i++) {
            for (int j = 0; j < outputHandler.getSlots(); j++) {
                ItemStack result = insertIntoOutput(outputHandler, j, outputs[i].copy(), false);
                if (result.isEmpty()) {
                    break;
                } else {
                    outputs[i] = result;
                }
                if (j == outputHandler.getSlots() - 1) {
                    notExported.add(result);
                }
            }
        }
        return notExported.toArray(new ItemStack[0]);
    }

    @Override
    public LazyOptional<IItemHandler> forSide(Direction side) {
        return LazyOptional.of(() -> new SidedCombinedInvWrapper(side, tile.coverHandler.map(c -> c).orElse(null), this::allowsInput, this::allowsOutput, this.inventories.values().stream().filter(t -> !(t instanceof FakeTrackedItemHandler)).toArray(IItemHandlerModifiable[]::new)));
    }

    @Override
    public LazyOptional<? extends IItemHandler> forNullSide() {
        return LazyOptional.of(() -> new ROCombinedInvWrapper(this.inventories.values().stream().filter(t -> !(t instanceof FakeTrackedItemHandler)).toArray(IItemHandlerModifiable[]::new)));
    }
}
