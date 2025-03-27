package org.gtreimagined.gtlib.integration.top;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.elements.ElementProgress;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tesseract.api.forge.TesseractCaps;
import tesseract.api.gt.IEnergyHandler;

public class EnergyInfoProvider implements IProbeInfoProvider {
    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(Ref.ID + ":energy_info");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level,
                             BlockState blockState, IProbeHitData data) {
        if (blockState.hasBlockEntity()) {
            BlockEntity tile = Utils.getTile(level, data.getPos());

            IEnergyHandler energyHandler = tile.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY).resolve().orElse(null);
            if (energyHandler != null) {
                long maxCapacity = energyHandler.getCapacity();
                if (maxCapacity == 0) return;

                IProbeInfo horizontalPane = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                //horizontalPane.text("Energy: ");
                NumberFormat format = player.isCrouching() ? NumberFormat.FULL : NumberFormat.COMPACT;
                horizontalPane.progress(energyHandler.getEnergy(), maxCapacity, probeInfo.defaultProgressStyle()
                        .suffix(Utils.literal(" / ").append(ElementProgress.format(maxCapacity, format, Utils.literal(" EU"))))
                        .filledColor(0xFF00FFFF)
                        .alternateFilledColor(0xFF009999)
                        .borderColor(0xFF555555).numberFormat(format));
            }
        }

    }
}
