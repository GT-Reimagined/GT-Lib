package org.gtreimagined.gtlib.util;

import org.gtreimagined.gtlib.capability.fluid.CauldronWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE;

public class FluidUtils {
    public static ResourceLocation getStillTexture(Fluid fluid){
        return fluid.getAttributes().getStillTexture();
    }

    public static ResourceLocation getFlowingTexture(Fluid fluid){
        return fluid.getAttributes().getFlowingTexture();
    }

    public static int getFluidTemperature(Fluid fluid){
        return fluid.getAttributes().getTemperature();
    }

    public static int getFluidDensity(Fluid fluid){
        return fluid.getAttributes().getDensity();
    }

    public static boolean isFluidGaseous(Fluid fluid){
        return fluid.getAttributes().isGaseous();
    }

    public static int getFluidColor(Fluid fluid){
        return fluid.getAttributes().getColor();
    }

    public static SoundEvent getFillSound(Fluid fluid){
        return fluid.getAttributes().getFillSound();
    }

    public static SoundEvent getEmptySound(Fluid fluid){
        return fluid.getAttributes().getEmptySound();
    }

    public static Component getFluidDisplayName(FluidStack fluid){
        return fluid.getDisplayName();
    }

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

    public static LazyOptional<IFluidHandler> getFluidHandler(Level level, BlockPos pos, Direction side){
       return getFluidHandler(level, pos, level.getBlockEntity(pos), side);
    }

    public static boolean fillItemFromContainer(int maxFill, ItemStack stack, IFluidHandler handler, Consumer<ItemStack> consumer){
        return fillItemFromContainer(maxFill, stack, handler, s -> true, consumer);
    }

    public static boolean emptyItemIntoContainer(int maxFill, ItemStack stack, IFluidHandler handler, Consumer<ItemStack> consumer){
        return emptyItemIntoContainer(maxFill, stack, handler, s -> true, consumer);
    }

    public static boolean fillItemFromContainer(int maxFill, ItemStack stack, IFluidHandler handler, Predicate<ItemStack> tester, Consumer<ItemStack> consumer){
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

    public static boolean emptyItemIntoContainer(int maxDrain, ItemStack stack, IFluidHandler handler, Predicate<ItemStack> tester, Consumer<ItemStack> consumer){
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

    public static FluidStack fromTag(CompoundTag tag){
        if (tag == null) {
            return FluidStack.EMPTY;
        }
        if (tag.contains("Fluid", Tag.TAG_STRING)) {
            tag.putString("FluidName", tag.getString("Fluid"));
            tag.remove("Fluid");
        }
        if (tag.contains("Nbt", Tag.TAG_COMPOUND)) {
            tag.put("Tag", tag.getCompound("Nbt"));
            tag.remove("Nbt");
        }
        return FluidStack.loadFluidStackFromNBT(tag);
    }
}
