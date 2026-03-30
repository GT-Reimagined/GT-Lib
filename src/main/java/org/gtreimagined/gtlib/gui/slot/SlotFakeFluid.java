package org.gtreimagined.gtlib.gui.slot;

import org.gtreimagined.gtlib.capability.FluidHandler.FluidTankType;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.gui.SlotType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.jetbrains.annotations.NotNull;


public class SlotFakeFluid extends AbstractSlot<SlotFakeFluid> {

    public final FluidTankType dir;

    public SlotFakeFluid(SlotType<SlotFakeFluid> type, IGuiHandler tile, FluidTankType dir, int index) {
        super(type, tile, new EmptyHandler(), index);
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
