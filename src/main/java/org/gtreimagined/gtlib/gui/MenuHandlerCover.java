package org.gtreimagined.gtlib.gui;

import org.gtreimagined.gtlib.capability.ICoverHandlerProvider;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.gui.container.ContainerCover;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class MenuHandlerCover<T extends ContainerCover> extends MenuHandler<T> {

    public MenuHandlerCover(String domain, String id) {
        super(domain, id);
    }

    @Override
    public T onContainerCreate(int windowId, Inventory inv, FriendlyByteBuf data) {
        BlockEntity tile = Utils.getTileFromBuf(data);
        if (tile instanceof ICoverHandlerProvider<?> provider) {
            Direction dir = Direction.from3DDataValue(data.readInt());
            var coverHandler = provider.getCoverHandler();
            return menu(coverHandler.map(ch -> ch.get(dir)).orElse(ICover.empty), inv, windowId);
        }
        return null;
    }
}
