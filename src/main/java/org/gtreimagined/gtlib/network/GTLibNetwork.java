package org.gtreimagined.gtlib.network;

import com.teamresourceful.resourcefullib.common.networking.NetworkChannel;
import com.teamresourceful.resourcefullib.common.networking.base.NetworkDirection;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.network.packets.ClientboundWorldgenSyncPacket;
import org.gtreimagined.gtlib.network.packets.FakeTilePacket;

public abstract class GTLibNetwork {

    public static final NetworkChannel NETWORK = new NetworkChannel(Ref.ID, 0, "main");

    public static final ResourceLocation TILE_GUI_PACKET_ID = new ResourceLocation(Ref.ID, "tile_gui");
    public static final ResourceLocation COVER_GUI_PACKET_ID = new ResourceLocation(Ref.ID, "cover_gui");
    public static final ResourceLocation FAKE_TILE_PACKET_ID = new ResourceLocation(Ref.ID, "fake_tile");
    public static final ResourceLocation WORLDGEN_SYNC_PACKET_ID = new ResourceLocation(Ref.ID, "worldgen_sync");
    public static final ResourceLocation STRUCTURE_CHECK_PACKET_ID = new ResourceLocation(Ref.ID, "structure_check");

    public static void register(){
        NETWORK.registerPacket(NetworkDirection.SERVER_TO_CLIENT, FAKE_TILE_PACKET_ID, FakeTilePacket.HANDLER, FakeTilePacket.class);
        NETWORK.registerPacket(NetworkDirection.SERVER_TO_CLIENT, WORLDGEN_SYNC_PACKET_ID, ClientboundWorldgenSyncPacket.HANDLER, ClientboundWorldgenSyncPacket.class);
    }
}
