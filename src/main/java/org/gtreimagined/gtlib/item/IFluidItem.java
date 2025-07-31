package org.gtreimagined.gtlib.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.function.Predicate;

public interface IFluidItem {

    int getCapacity();

    default Item getItem(){
        return (Item)this;
    }

    default FluidStack getTank(ItemStack stack) {
        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(f -> f.getFluidInTank(0)).orElse(FluidStack.EMPTY);
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

    default ItemStack fill(Fluid fluid, int amount) {
        ItemStack stack = new ItemStack(getItem());
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(f -> f.fill(new FluidStack(fluid, amount), FluidAction.EXECUTE));
        return stack;
    }

    default ItemStack fill(Fluid fluid) {
        return fill(fluid, this.getCapacity());
    }

    default ItemStack drain(ItemStack old, FluidStack fluid) {
        old.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(f -> f.drain(fluid, FluidAction.EXECUTE));
        return old;
    }

    Predicate<FluidStack> getFilter();
}
