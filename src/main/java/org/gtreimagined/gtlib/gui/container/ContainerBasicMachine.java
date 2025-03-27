package org.gtreimagined.gtlib.gui.container;

import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.gui.MenuHandlerMachine;
import net.minecraft.world.entity.player.Inventory;

public class ContainerBasicMachine<T extends BlockEntityMachine<T>> extends ContainerMachine<T> {

    public ContainerBasicMachine(T tile, Inventory playerInv, MenuHandlerMachine<T, ContainerMachine<T>> handler, int windowId) {
        super(tile, playerInv, handler, windowId);
    }
}
