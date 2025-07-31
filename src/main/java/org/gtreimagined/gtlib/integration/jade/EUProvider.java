package org.gtreimagined.gtlib.integration.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.tesseract.api.forge.TesseractCaps;
import org.gtreimagined.tesseract.api.eu.IEnergyHandler;
import org.gtreimagined.tesseract.api.hu.IHeatHandler;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.IProgressStyle;

public class EUProvider implements IBlockComponentProvider, IServerDataProvider<BlockEntity> {
    private static final ResourceLocation ID = new ResourceLocation(Ref.ID, "eu_hu");
    public static EUProvider INSTANCE = new EUProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        if (blockEntity == null) return;
        if (Minecraft.getInstance().player == null) return;
        {
            IEnergyHandler storage = blockEntity.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY).orElse(null);
            if (storage != null && (!accessor.isServerConnected() || accessor.getServerData().contains("jadeEU"))) {
                IElementHelper helper = tooltip.getElementHelper();
                long cur, max;
                if (accessor.isServerConnected()) {
                    cur = accessor.getServerData().getLong("jadeEU");
                    max = accessor.getServerData().getLong("jadeMaxEU");
                } else {
                    cur = storage.getEnergy();
                    max = storage.getCapacity();
                }
                String curText = ChatFormatting.WHITE + humanReadableNumber(cur, "EU", Minecraft.getInstance().player.isCrouching()) + ChatFormatting.GRAY;
                String maxText = humanReadableNumber(max, "EU", Minecraft.getInstance().player.isCrouching());
                MutableComponent text = Utils.translatable("jade.fe", curText, maxText).withStyle(ChatFormatting.WHITE);
                IProgressStyle progressStyle = helper.progressStyle().color(0xFF00FFFF, 0xFF009999);
                tooltip.add(helper.progress((float) cur / max, text, progressStyle, BoxStyle.DEFAULT, true));
            }
        }
        {
            IHeatHandler storage = blockEntity.getCapability(TesseractCaps.HEAT_CAPABILITY).orElse(null);
            if (storage != null && (!accessor.isServerConnected() || accessor.getServerData().contains("jadeHU"))) {
                IElementHelper helper = tooltip.getElementHelper();
                long cur, max;
                if (accessor.isServerConnected()) {
                    cur = accessor.getServerData().getLong("jadeHU");
                    max = accessor.getServerData().getLong("jadeMaxHU");
                } else {
                    cur = storage.getHeat();
                    max = storage.getHeatCap();
                }
                String curText = ChatFormatting.WHITE + humanReadableNumber(cur, "HU", Minecraft.getInstance().player.isCrouching()) + ChatFormatting.GRAY;
                String maxText = humanReadableNumber(max, "HU", Minecraft.getInstance().player.isCrouching());
                MutableComponent text = Utils.translatable("jade.fe", curText, maxText).withStyle(ChatFormatting.WHITE);
                IProgressStyle progressStyle = helper.progressStyle().color(0xFFFF0000, 0xFF660000);
                tooltip.add(helper.progress((float) cur / max, text, progressStyle, BoxStyle.DEFAULT, true));
            }
        }
    }

    private String humanReadableNumber(double number, String unit, boolean shift) {
        return shift ? number + unit : IDisplayHelper.get().humanReadableNumber(number, unit, false);
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, ServerPlayer serverPlayer, Level level, BlockEntity blockEntity, boolean b) {
        IEnergyHandler handler = blockEntity.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY).orElse(null);
        if (handler != null) {
            compoundTag.putLong("jadeEU", handler.getEnergy());
            compoundTag.putLong("jadeMaxEU", handler.getCapacity());
        }
        IHeatHandler heatHandler = blockEntity.getCapability(TesseractCaps.HEAT_CAPABILITY).orElse(null);
        if (heatHandler != null) {
            compoundTag.putLong("jadeHU", heatHandler.getHeat());
            compoundTag.putLong("jadeMaxHU", heatHandler.getHeatCap());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
