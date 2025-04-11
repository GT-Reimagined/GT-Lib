package org.gtreimagined.gtlib.network.packets;

import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.network.GTLibNetwork;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOreData;
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerData;
import org.gtreimagined.gtlib.worldgen.vanillaore.VanillaVeinData;
import org.gtreimagined.gtlib.worldgen.vein.VeinData;

public class ClientboundWorldgenSyncPacket implements Packet<ClientboundWorldgenSyncPacket> {
    public static ClientHandler HANDLER = new ClientHandler();
    @Override
    public ResourceLocation getID() {
        return GTLibNetwork.WORLDGEN_SYNC_PACKET_ID;
    }

    @Override
    public PacketHandler<ClientboundWorldgenSyncPacket> getHandler() {
        return HANDLER;
    }

    private static class ClientHandler implements PacketHandler<ClientboundWorldgenSyncPacket> {

        @Override
        public void encode(ClientboundWorldgenSyncPacket clientboundWorldgenSyncPacket, FriendlyByteBuf friendlyByteBuf) {
            VeinData.INSTANCE.encodeVeins(friendlyByteBuf);
            VanillaVeinData.INSTANCE.encodeVeins(friendlyByteBuf);
            SmallOreData.INSTANCE.encodeVeins(friendlyByteBuf);
            StoneLayerData.INSTANCE.encodeVeins(friendlyByteBuf);
        }

        @Override
        public ClientboundWorldgenSyncPacket decode(FriendlyByteBuf friendlyByteBuf) {
            VeinData.INSTANCE.updateVeins(VeinData.INSTANCE.decodeVeins(friendlyByteBuf));
            VanillaVeinData.INSTANCE.updateVeins(VanillaVeinData.INSTANCE.decodeVeins(friendlyByteBuf));
            SmallOreData.INSTANCE.updateVeins(SmallOreData.INSTANCE.decodeVeins(friendlyByteBuf));
            StoneLayerData.INSTANCE.updateVeins(StoneLayerData.INSTANCE.decodeVeins(friendlyByteBuf));
            return new ClientboundWorldgenSyncPacket();
        }

        @Override
        public PacketContext handle(ClientboundWorldgenSyncPacket clientboundWorldgenSyncPacket) {
            return (player, level) -> {};
        }
    }
}
