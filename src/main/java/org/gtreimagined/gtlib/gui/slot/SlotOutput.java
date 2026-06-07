package org.gtreimagined.gtlib.gui.slot;

import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.gui.SlotType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;


public class SlotOutput extends AbstractSlot<SlotOutput> {

    public SlotOutput(SlotType<SlotOutput> type, IGuiHandler tile, IItemHandler stackHandler, int index) {
        super(type, tile, stackHandler, index);
    }

    @Override
    public boolean mayPickup(Player player) {
        return true;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }
}
