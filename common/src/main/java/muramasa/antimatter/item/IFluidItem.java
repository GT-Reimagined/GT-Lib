package muramasa.antimatter.item;

import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import earth.terrarium.botarium.common.fluid.base.BotariumFluidItem;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.common.fluid.impl.WrappedItemFluidContainer;
import earth.terrarium.botarium.common.fluid.utils.FluidHooks;
import earth.terrarium.botarium.common.item.ItemStackHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface IFluidItem {

    int getCapacity();

    default FluidStack getTank(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(f -> f.getFluidInTank(0)).orElse(FluidStack.EMPTY);
    }

    default int getFluidAmount(ItemStack stack) {
        return getTank(stack).getAmount();
    }

    default Fluid getFluid(ItemStack stack) {
        return getTank(stack).getFluid();
    }

    default FluidStack getFluidStack(ItemStack stack){
        return getTank(stack).copy();
    }

    default void insert(ItemStack stack, FluidStack fluid) {
        stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(f -> f.fill(fluid, FluidAction.EXECUTE));
    }

    default void extract(ItemStack stack, FluidStack fluid) {
        stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(f -> f.drain(fluid, FluidAction.EXECUTE));
    }

    Predicate<FluidStack> getFilter();
}
