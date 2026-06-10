package org.gtreimagined.gtlib.mui.factory;

import brachy.modularui.api.IUIHolder;
import brachy.modularui.factory.AbstractUIFactory;
import brachy.modularui.factory.GuiManager;
import brachy.modularui.factory.SidedPosGuiData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.capability.ICoverHandlerProvider;
import org.gtreimagined.gtlib.cover.ICover;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CoverUIFactory extends AbstractUIFactory<SidedPosGuiData> {
    public static final CoverUIFactory INSTANCE = new CoverUIFactory();
    protected CoverUIFactory() {
        super(new ResourceLocation(Ref.ID, "cover"));
    }

    public void open(ServerPlayer player, ICover cover) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(cover);
        if (player.level() != cover.source().getTile().getLevel()) {
            throw new IllegalArgumentException("Cover must be in same dimension as the player!");
        }
        BlockPos pos = cover.source().getTile().getBlockPos();
        Direction side = cover.side();
        SidedPosGuiData data = new SidedPosGuiData(player, pos, side);
        GuiManager.open(this, data, player);
    }

    @Override
    public @NotNull IUIHolder<SidedPosGuiData> getGuiHolder(SidedPosGuiData data) {
        BlockEntity be = data.getBlockEntity();
        if (be == null) {
            throw new IllegalStateException("Could not get gui for null BlockEntity!");
        }
        if (!(be instanceof ICoverHandlerProvider<?> provider) || provider.getCoverHandler().isEmpty()){
            throw new IllegalStateException("Could not get CoverHolder for found BlockEntity!");
        }
        ICover cover = provider.getCoverHandler().get().get(data.getSide());
        if (cover == null) {
            throw new IllegalStateException("Could not find cover at side " + data.getSide() +
                    " for found CoverHolder!");
        }
        return cover;
    }

    @Override
    public void writeGuiData(SidedPosGuiData sidedPosGuiData, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(sidedPosGuiData.getBlockPos());
        friendlyByteBuf.writeByte(sidedPosGuiData.getSide().get3DDataValue());
    }

    @Override
    public @NotNull SidedPosGuiData readGuiData(Player player, FriendlyByteBuf friendlyByteBuf) {
        return new SidedPosGuiData(player, friendlyByteBuf.readBlockPos(), Direction.from3DDataValue(friendlyByteBuf.readByte()));
    }
}
