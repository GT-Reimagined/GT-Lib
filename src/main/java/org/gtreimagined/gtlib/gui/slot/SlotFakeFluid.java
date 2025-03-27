package org.gtreimagined.gtlib.gui.slot;

import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.capability.machine.MachineFluidHandler;
import org.gtreimagined.gtlib.gui.SlotType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.jetbrains.annotations.NotNull;


public class SlotFakeFluid extends AbstractSlot<SlotFakeFluid> {

    public final MachineFluidHandler.FluidDirection dir;

    public SlotFakeFluid(SlotType<SlotFakeFluid> type, IGuiHandler tile, MachineFluidHandler.FluidDirection dir, int index, int x, int y) {
        super(type, tile, new EmptyHandler(), index, x, y);
        this.dir = dir;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return false;
    }
}
