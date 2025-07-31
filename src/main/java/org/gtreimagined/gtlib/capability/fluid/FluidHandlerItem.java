package org.gtreimagined.gtlib.capability.fluid;

import org.gtreimagined.gtlib.util.FluidUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class FluidHandlerItem extends FluidHandlerItemStack {
    final Predicate<FluidStack> filter;
    public FluidHandlerItem(@NotNull ItemStack container, int capacity, Predicate<FluidStack> filter) {
        super(container, capacity);
        this.filter = filter;
    }

    @Override
    public @NotNull FluidStack getFluid() {
        CompoundTag tagCompound = this.container.getTag();
        if (tagCompound != null && tagCompound.contains("BotariumData")){
           CompoundTag botTag = tagCompound.getCompound("BotariumData");
           if (botTag.contains("StoredFluids")){
               ListTag storedFluids = botTag.getList("StoredFluids", Tag.TAG_COMPOUND);
               if (!storedFluids.isEmpty()){
                   CompoundTag fluidTag = storedFluids.getCompound(0);
                   FluidStack fluidStack = FluidUtils.fromTag(fluidTag);
                   if (!fluidStack.isEmpty()){
                       botTag.remove("StoredFluids");
                       if (botTag.isEmpty()){
                           tagCompound.remove("BotariumData");
                       }
                       this.setFluid(fluidStack);
                   }
               }
           }
        }
        return super.getFluid();
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return super.canFillFluidType(fluid) && filter.test(fluid);
    }
}
