package muramasa.antimatter.gui.slot;

import muramasa.antimatter.Data;
import muramasa.antimatter.capability.IGuiHandler;
import muramasa.antimatter.gui.SlotType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class SlotFluidDisplaySettable extends SlotFake {
    public SlotFluidDisplaySettable(SlotType<SlotFake> type, IGuiHandler tile, IItemHandler stackHandler, int index, int x, int y) {
        super(type, tile, stackHandler, index, x, y, true);
    }

    @Override
    public ItemStack clickSlot(int clickedButton, ClickType clickType, Player playerEntity, AbstractContainerMenu container) {
        if (container.getCarried().isEmpty() || container.getCarried().getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(f -> !f.getFluidInTank(0).isEmpty()).orElse(false)){
            return super.clickSlot(clickedButton, clickType, playerEntity, container);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void set(@NotNull ItemStack stack) {
        if (!stack.isEmpty()){
            ItemStack[] stacks = new ItemStack[1];
            stacks[0] = ItemStack.EMPTY;
            stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(f -> {
                FluidStack fluidStack = f.getFluidInTank(0);
                if (!fluidStack.isEmpty()){
                    stacks[0] = Data.FLUID_ICON.fill(fluidStack.getFluid());
                }
            });
            super.set(stacks[0]);
            return;
        }
        super.set(stack);
    }
}
