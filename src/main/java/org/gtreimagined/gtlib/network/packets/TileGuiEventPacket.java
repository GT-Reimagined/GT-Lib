package org.gtreimagined.gtlib.network.packets;

import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.gui.container.IGTContainer;
import org.gtreimagined.gtlib.gui.event.IGuiEvent;
import org.gtreimagined.gtlib.network.GTLibNetwork;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TileGuiEventPacket extends AbstractGuiEventPacket<TileGuiEventPacket> {
    public static final PacketHandler<TileGuiEventPacket> HANDLER = new Handler();

    public TileGuiEventPacket(IGuiEvent event, BlockPos pos) {
        super(event, pos, GTLibNetwork.TILE_GUI_PACKET_ID);
    }

    @Override
    public PacketHandler<TileGuiEventPacket> getHandler() {
        return HANDLER;
    }

    public static TileGuiEventPacket decode(FriendlyByteBuf buf) {
        return new TileGuiEventPacket(IGuiEvent.IGuiEventFactory.read(buf), buf.readBlockPos());
    }

    private static class Handler implements PacketHandler<TileGuiEventPacket> {

        @Override
        public void encode(TileGuiEventPacket msg, FriendlyByteBuf buf) {
            msg.event.getFactory().write(msg.event, buf);
            buf.writeBlockPos(msg.pos);
        }

        @Override
        public TileGuiEventPacket decode(FriendlyByteBuf buf) {
            return new TileGuiEventPacket(IGuiEvent.IGuiEventFactory.read(buf), buf.readBlockPos());
        }

        @Override
        public PacketContext handle(TileGuiEventPacket msg) {
            return (sender, level) -> {
                if (sender != null) {
                    BlockEntity tile = Utils.getTile(sender.level(), msg.pos);
                    if (tile instanceof IGuiHandler) {
                        if (msg.event.forward()) {
                            ((IGuiHandler) tile).onGuiEvent(msg.event, sender);
                        } else {
                            msg.event.handle(sender, ((IGTContainer) sender.containerMenu).source());
                        }
                    }
                }
            };
        }
    }
}
