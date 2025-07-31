package org.gtreimagined.gtlib.integration.top;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.machine.MachineRecipeHandler;
import org.gtreimagined.gtlib.machine.MachineFlag;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RecipeInfoProvider implements IProbeInfoProvider {
    @Override
    public ResourceLocation getID() {
        return new ResourceLocation(Ref.ID + ":recipe_info");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level,
                             BlockState blockState, IProbeHitData data) {

        if (blockState.hasBlockEntity()) {
            BlockEntity tile = Utils.getTile(level, data.getPos());

            if (tile instanceof BlockEntityMachine<?> machine) {
                MachineRecipeHandler<?> recipeHandler = machine.recipeHandler.orElse(null);
                if(recipeHandler == null) {
                    return;
                }

                long currentProgress = recipeHandler.isGenerator() ? recipeHandler.getPowerGenerated() : recipeHandler.getCurrentProgress();
                long maxProgress = recipeHandler.isGenerator() ? recipeHandler.getTotalPowerToGenerate() : recipeHandler.getMaxProgress();
                String text;

                if (recipeHandler.isGenerator()) {
                    text = " /" + maxProgress + " " + (machine.has(MachineFlag.EU) ? "EU" : machine.has(MachineFlag.FE) ? "FE" : "");
                } else if (maxProgress < 20) {
                    text = " / " + maxProgress + " t";
                } else {
                    // Display progress as seconds if it's greater 1 second
                    currentProgress = Math.round(currentProgress / 20.0F);
                    maxProgress = Math.round(maxProgress / 20.0F);
                    text = " / " + maxProgress + " s";
                }

                if(maxProgress > 0 && machine.getMachineState() == MachineState.ACTIVE) {
                    IProbeInfo horizontalPane = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                    horizontalPane.text(recipeHandler.isGenerator() ? "Power Generated" : "Progress: ");
                    horizontalPane.progress(currentProgress, maxProgress, probeInfo.defaultProgressStyle()
                            .suffix(text)
                            .filledColor(0xFF4CBB17)
                            .alternateFilledColor(0xFF4CBB17)
                            .borderColor(0xFF555555));
                }

            }

        }

    }
}
