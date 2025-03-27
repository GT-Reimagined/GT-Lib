package org.gtreimagined.gtlib.gui;

import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.gui.container.ContainerMachine;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class MenuHandlerMachine<T extends BlockEntityMachine<T>, U extends ContainerMachine<T>> extends MenuHandler<U> {

    public MenuHandlerMachine(String domain, String id) {
        super(domain, id);
    }

    @Override
    public U onContainerCreate(int windowId, Inventory inv, FriendlyByteBuf data) {
        BlockEntity tile = Utils.getTileFromBuf(data);
        boolean isMachine = tile instanceof BlockEntityMachine;
        if (isMachine) {
            BlockEntityMachine<?> machine = (BlockEntityMachine) tile;
            return menu(machine, inv, windowId);
        }
        return null;
    }
}
