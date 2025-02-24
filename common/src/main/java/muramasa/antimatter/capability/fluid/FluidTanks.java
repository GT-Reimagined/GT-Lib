package muramasa.antimatter.capability.fluid;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import muramasa.antimatter.blockentity.BlockEntityBase;
import muramasa.antimatter.capability.IMachineHandler;
import muramasa.antimatter.machine.event.IMachineEvent;
import muramasa.antimatter.util.FluidPlatformUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * Array of multiple instances of FluidTank
 */
public class FluidTanks implements IFluidHandler {

    public static <T extends BlockEntityBase<T> & IMachineHandler> FluidTanks create(T tile, IMachineEvent contentEvent,
                                                                                     UnaryOperator<Builder<T>> builder) {
        return builder.apply(new Builder<>(tile, contentEvent)).build();
    }

    private final FluidTank[] tanks;
    @Getter
    private final int totalCapacity;

    public FluidTanks(int tanks, int tankAmountInMB) {
        this.tanks = new FluidTank[tanks];
        for (int i = 0; i < tanks; i++) {
            this.tanks[i] = new FluidTank(tankAmountInMB);
        }
        this.totalCapacity = tanks * tankAmountInMB;
    }

    public FluidTanks(int tanks, int tankAmountInMB, Predicate<FluidStack> validator) {
        this.tanks = new FluidTank[tanks];
        for (int i = 0; i < tanks; i++) {
            this.tanks[i] = new FluidTank(tankAmountInMB, validator);
        }
        this.totalCapacity = tanks * tankAmountInMB;
    }

    public FluidTanks(int... tankAmountsInMB) {
        this.tanks = new FluidTank[tankAmountsInMB.length];
        for (int i = 0; i < this.tanks.length; i++) {
            this.tanks[i] = new FluidTank(tankAmountsInMB[i]);
        }
        this.totalCapacity = IntStream.of(tankAmountsInMB).sum();
    }

    public FluidTanks(Collection<FluidTank> tanks) {
        this.tanks = tanks.toArray(new FluidTank[0]);
        this.totalCapacity = tanks.stream().mapToInt(FluidTank::getCapacity).sum();
    }

    public FluidTanks(@NotNull FluidTank... tanks) {
        this.tanks = tanks;
        this.totalCapacity = Arrays.stream(tanks).mapToInt(FluidTank::getCapacity).sum();
    }

    public int getFirstAvailableTank(FluidStack stack, boolean drain) {
        int firstAvailable = -1;
        int firstEmpty = -1;
        for (int i = 0; i < tanks.length; i++) {
            FluidTank tank = this.tanks[i];
            if (!drain){
                if (tank.isEmpty() && tank.isFluidValid(stack) && firstEmpty == -1){
                    firstEmpty = i;
                }
            }
            if (tank.getFluid().isFluidEqual(stack)) {
                firstAvailable = i;
                break;
            }
        }
        if (firstAvailable == -1) return firstEmpty;
        return firstAvailable;
    }

    public FluidTank getTank(int tank) {
        return this.tanks[tank];
    }

    public List<FluidStack> getFluids() {
        return Arrays.stream(this.tanks).map(FluidTank::getFluid).toList();
    }

    public FluidTank[] getBackingTanks() {
        return tanks;
    }


    @Override
    public int getTanks() {
        return tanks.length;
    }

    public boolean isEmpty() {
        boolean hasFluid = false;
        for (int i = 0; i < getTanks(); i++) {
            if (!getTank(i).isEmpty()){
                hasFluid = true;
            }
        }
        return !hasFluid;
    }

    @NotNull
    public FluidStack getFluidInTank(int tank) {
        return this.tanks[tank].getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.tanks[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.tanks[tank].isFluidValid(stack);
    }

    public int getTotalFluidAmount() {
        int amount = 0;
        for (FluidTank tank : tanks) {
            amount += tank.getFluid().getAmount();
        }
        return amount;
    }

    @Override
    public int fill(FluidStack fluid, FluidAction action) {
        int tank = getFirstAvailableTank(fluid, false);
        if (tank == -1) return 0;
        return getTank(tank).fill(fluid, action);
    }

    @Override
    public FluidStack drain(FluidStack fluid, FluidAction action) {
        for (int i = 0; i < tanks.length; i++) {
            FluidStack drain = getTank(i).drain(fluid, action);
            if (!drain.isEmpty())
                return drain;
        }
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int amount, FluidAction action) {
        for (int i = 0; i < tanks.length; i++) {
            FluidStack drain = getTank(i).drain(amount, action);
            if (!drain.isEmpty())
                return drain;
        }
        return FluidStack.EMPTY;
    }

    public void setFluid(int slot, FluidStack fluid) {
        tanks[slot].setFluid(fluid);
    }

    public ListTag serialize() {
        ListTag nbt = new ListTag();
        Arrays.stream(tanks).forEach(t -> nbt.add(t.getFluid().writeToNBT(new CompoundTag())));
        return nbt;
    }

    public void deserialize(ListTag nbt) {
        int i = 0;
        for (Tag tank : nbt) {
            if (tank instanceof CompoundTag cnbt) {
                if (i > tanks.length - 1)
                    break;
                tanks[i++].setFluid(FluidPlatformUtils.fromTag(cnbt));
            }
        }
    }

    public static class Builder<T extends BlockEntityBase & IMachineHandler> {

        private final T tile;
        private final List<FluidTank> tanks;
        private final IMachineEvent contentEvent;

        private Builder(T tile, IMachineEvent contentEvent) {
            this.tile = tile;
            this.tanks = new ObjectArrayList<>();
            this.contentEvent = contentEvent;
        }

        public Builder<T> tank(Predicate<FluidStack> validator, int amountInMB) {
            this.tanks.add(new FluidTank(amountInMB, validator) {
                @Override
                protected void onContentsChanged() {
                    tile.onMachineEvent(contentEvent, this.fluid);
                }
            });
            return this;
        }

        public Builder<T> tank(int amountInMB) {
            this.tanks.add(new FluidTank(amountInMB) {
                @Override
                protected void onContentsChanged() {
                    tile.onMachineEvent(contentEvent, this.fluid);
                }
            });
            return this;
        }

        private FluidTanks build() {
            return new FluidTanks(this.tanks);
        }

    }

}
