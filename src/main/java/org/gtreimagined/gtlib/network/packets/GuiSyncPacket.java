package org.gtreimagined.gtlib.network.packets;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import io.netty.buffer.ByteBuf;
import org.gtreimagined.gtlib.gui.GuiInstance;

import java.util.List;

public abstract class GuiSyncPacket<T extends GuiSyncPacket<T>> implements Packet<T> {
    GuiInstance.SyncHolder[] data;
    public ByteBuf clientData;

    public GuiSyncPacket(final List<GuiInstance.SyncHolder> data) {
        this.data = data.toArray(new GuiInstance.SyncHolder[0]);
    }

    public GuiSyncPacket(final ByteBuf data) {
        this.clientData = data;

    }
}
