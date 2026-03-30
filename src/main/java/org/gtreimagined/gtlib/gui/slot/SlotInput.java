package org.gtreimagined.gtlib.gui.slot;

import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.gui.SlotType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandler;

public class SlotInput extends AbstractSlot<SlotInput> {

    public SlotInput(SlotType<SlotInput> type, IGuiHandler tile, IItemHandler stackHandler, int index) {
        super(type, tile, stackHandler, index);
    }

    @Override
    public boolean mayPickup(Player player) {
        return true;
    }
}
