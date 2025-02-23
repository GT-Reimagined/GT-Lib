package muramasa.antimatter.util;

import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.base.PlatformFluidHandler;
import earth.terrarium.botarium.common.fluid.base.PlatformFluidItemHandler;
import earth.terrarium.botarium.common.fluid.utils.FluidHooks;
import earth.terrarium.botarium.common.item.ItemStackHolder;
import earth.terrarium.botarium.forge.fluid.ForgeFluidHandler;
import muramasa.antimatter.capability.fluid.CauldronWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public abstract class FluidPlatformUtils {
    public static FluidPlatformUtils INSTANCE; // =  ServiceLoader.load(FluidPlatformUtils.class).findFirst().orElseThrow(() -> new IllegalStateException("No implementation of FluidPlatformUtils found"));
    //public static FluidPlatformUtils INSTANCE = new FluidPlatformUtils();

    public static FluidHolder createFluidStack(Fluid fluid, long amount){
        return FluidHooks.newFluidHolder(fluid,amount, null);
    }

    public abstract ResourceLocation getStillTexture(Fluid fluid);

    public abstract ResourceLocation getFlowingTexture(Fluid fluid);

    public abstract ResourceLocation getFluidId(Fluid fluid);

    public abstract int getFluidTemperature(Fluid fluid);

    public abstract int getFluidDensity(Fluid fluid);

    public abstract boolean isFluidGaseous(Fluid fluid);

    public abstract int getFluidColor(Fluid fluid);

    public abstract SoundEvent getFluidSound(Fluid fluid, boolean fill);

    public abstract Component getFluidDisplayName(FluidStack fluid);

    public static LazyOptional<IFluidHandler> getFluidHandler(Level level, BlockPos pos, @Nullable BlockEntity be, Direction side){
        if (be == null){
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof AbstractCauldronBlock){
                return LazyOptional.of(() ->new CauldronWrapper(state, level, pos));
            }
            return LazyOptional.empty();
        }
        return be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }

    /**
     * Fill a destination fluid handler from a source fluid handler with a max amount.
     * To specify a fluid to transfer instead of max amount, use {@link #tryFluidTransfer(PlatformFluidHandler, PlatformFluidHandler, FluidHolder, boolean)}
     * To transfer as much as possible, use {@link Integer#MAX_VALUE} for maxAmount.
     *
     * @param fluidDestination The fluid handler to be filled.
     * @param fluidSource      The fluid handler to be drained.
     * @param maxAmount        The largest amount of fluid that should be transferred.
     * @param doTransfer       True if the transfer should actually be done, false if it should be simulated.
     * @return the fluidStack that was transferred from the source to the destination. null on failure.
     */
    @NotNull
    public FluidHolder tryFluidTransfer(PlatformFluidHandler fluidDestination, PlatformFluidHandler fluidSource, long maxAmount, boolean doTransfer) {
        for (int i = 0; i < fluidSource.getTankAmount(); i++) {
            FluidHolder fluid = fluidSource.getFluidInTank(i);
            FluidHolder transfer = tryFluidTransfer(fluidDestination, fluidSource, fluid.copyWithAmount(Math.min(fluid.getFluidAmount(), maxAmount)), doTransfer);
            if (!transfer.isEmpty()) return transfer;
        }
        return FluidHooks.emptyFluid();
    }

    /**
     * Fill a destination fluid handler from a source fluid handler using a specific fluid.
     * To specify a max amount to transfer instead of specific fluid, use {@link #tryFluidTransfer(PlatformFluidHandler, PlatformFluidHandler, long, boolean)}
     * To transfer as much as possible, use {@link Integer#MAX_VALUE} for resource.amount.
     *
     * @param fluidDestination The fluid handler to be filled.
     * @param fluidSource      The fluid handler to be drained.
     * @param resource         The fluid that should be transferred. Amount represents the maximum amount to transfer.
     * @param doTransfer       True if the transfer should actually be done, false if it should be simulated.
     * @return the FluidHolder that was transferred from the source to the destination. null on failure.
     */
    @NotNull
    public FluidHolder tryFluidTransfer(PlatformFluidHandler fluidDestination, PlatformFluidHandler fluidSource, FluidHolder resource, boolean doTransfer)
    {
        FluidHolder drainable = fluidSource.extractFluid(resource, true);
        if (!drainable.isEmpty() && resource.matches(drainable))
        {
            return tryFluidTransfer_Internal(fluidDestination, fluidSource, drainable, doTransfer);
        }
        return FluidHooks.emptyFluid();
    }

    /**
     * Internal method for filling a destination fluid handler from a source fluid handler using a specific fluid.
     * Assumes that "drainable" can be drained from "fluidSource".
     *
     * Modders: Instead of this method, use {@link #tryFluidTransfer(PlatformFluidHandler, PlatformFluidHandler, FluidHolder, boolean)}
     * or {@link #tryFluidTransfer(PlatformFluidHandler, PlatformFluidHandler, long, boolean)}.
     */
    @NotNull
    private static FluidHolder tryFluidTransfer_Internal(PlatformFluidHandler fluidDestination, PlatformFluidHandler fluidSource, FluidHolder drainable, boolean doTransfer)
    {
        long fillableAmount = fluidDestination.insertFluid(drainable, true);
        if (fillableAmount > 0)
        {
            drainable.setAmount(fillableAmount);
            if (doTransfer)
            {
                FluidHolder drained = fluidSource.extractFluid(drainable, false);
                if (!drained.isEmpty())
                {
                    drained.setAmount(fluidDestination.insertFluid(drained, false));
                    return drained;
                }
            }
            else
            {
                return drainable;
            }
        }
        return FluidHooks.emptyFluid();
    }

    public boolean fillItemFromContainer(int maxFill, ItemStack stack, IFluidHandler handler, Consumer<ItemStack> consumer){
        return fillItemFromContainer(maxFill, stack, handler, s -> true, consumer);
    }

    public boolean emptyItemIntoContainer(int maxFill, ItemStack stack, IFluidHandler handler, Consumer<ItemStack> consumer){
        return emptyItemIntoContainer(maxFill, stack, handler, s -> true, consumer);
    }

    public boolean fillItemFromContainer(int maxFill, ItemStack stack, IFluidHandler handler, Predicate<ItemStack> tester, Consumer<ItemStack> consumer){
        IFluidHandlerItem itemHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).resolve().orElse(null);
        if (itemHandler == null) return false;
        final int actualMax = maxFill == -1 ? itemHandler.getTankCapacity(0) : maxFill;
        ItemStack checkContainer = stack.copy().getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(t -> {
            t.fill(FluidUtil.tryFluidTransfer(t, handler, actualMax, false), EXECUTE);
            return t.getContainer();
        }).orElse(ItemStack.EMPTY);
        if (!tester.test(checkContainer)) return false;
        FluidStack fluidStack = FluidUtil.tryFluidTransfer(itemHandler, handler, actualMax, true);
        if (!fluidStack.isEmpty()) {
            consumer.accept(checkContainer);
            return true;
        }
        return false;
    }

    public boolean emptyItemIntoContainer(int maxDrain, ItemStack stack, IFluidHandler handler, Predicate<ItemStack> tester, Consumer<ItemStack> consumer){
        IFluidHandlerItem itemHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).resolve().orElse(null);
        if (itemHandler == null) return false;
        final int actualMax = maxDrain == -1 ? itemHandler.getTankCapacity(0) : maxDrain;
        ItemStack checkContainer = stack.copy().getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(t -> {
            t.drain(actualMax, EXECUTE);
            return t.getContainer();
        }).orElse(ItemStack.EMPTY);
        if (!tester.test(checkContainer)) return false;
        FluidStack fluidStack = FluidUtil.tryFluidTransfer(handler, itemHandler, actualMax, true);
        if (!fluidStack.isEmpty()) {
            consumer.accept(checkContainer);
            return true;
        }
        return false;
    }

    public void writeToPacket(FriendlyByteBuf buffer, FluidHolder holder) {
        if (holder.isEmpty()) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeVarInt(Registry.FLUID.getId(holder.getFluid()));
            buffer.writeVarLong(holder.getFluidAmount());
            buffer.writeNbt(holder.getCompound());
        }
    }

    public FluidHolder readFromPacket(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) return FluidHooks.emptyFluid();
        Fluid fluid = Registry.FLUID.byId(buffer.readVarInt());
        long amount = buffer.readVarLong();
        return FluidHooks.newFluidHolder(fluid, amount, buffer.readNbt());
    }

    public FluidHolder fromTag(CompoundTag tag){
        if (tag == null) {
            return FluidHooks.emptyFluid();
        }
        if (!tag.contains("FluidName", Tag.TAG_STRING)) {
            return FluidHooks.fluidFromCompound(tag);
        }

        ResourceLocation fluidName = new ResourceLocation(tag.getString("FluidName"));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
        if (fluid == null) {
            return FluidHooks.emptyFluid();
        }
        FluidHolder stack = FluidHooks.newFluidHolder(fluid, tag.getInt("Amount"), null);
        if (tag.contains("Tag", Tag.TAG_COMPOUND)) {
            stack.setCompound(tag.getCompound("Tag"));
        }
        return stack;
    }
}
