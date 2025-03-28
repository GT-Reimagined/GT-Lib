package org.gtreimagined.gtlib.integration.jade;

import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.data.GTLibMaterials;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.addon.harvest.SimpleToolHandler;
import tesseract.Tesseract;

import static org.gtreimagined.gtlib.data.GTTools.WIRE_CUTTER;
import static org.gtreimagined.gtlib.data.GTTools.WRENCH;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    public static ResourceLocation EU = new ResourceLocation(Tesseract.API_ID, "eu");
    public static ResourceLocation HU = new ResourceLocation(Tesseract.API_ID, "hu");
    public static ResourceLocation PROGRESS = new ResourceLocation(Ref.ID, "machine_progress");
    public static ResourceLocation STRUCTURE = new ResourceLocation(Ref.ID, "structure_valid");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(EUProvider.INSTANCE, BlockEntity.class);
        registration.registerBlockDataProvider(MachineProvider.INSTANCE, BlockEntityMachine.class);
        registration.addConfig(EU, true);
        registration.addConfig(HU, true);
        registration.addConfig(PROGRESS, true);
        registration.addConfig(STRUCTURE, true);
        HarvestToolProvider.registerHandler(new SimpleToolHandler("wrench", WRENCH.getToolType(), WRENCH.getToolItem(GTLibMaterials.Iron), WRENCH.getToolItem(GTLibMaterials.Diamond), WRENCH.getToolItem(GTLibMaterials.NetherizedDiamond)));
        HarvestToolProvider.registerHandler(new SimpleToolHandler("wire_cutter", WIRE_CUTTER.getToolType(), WIRE_CUTTER.getToolItem(GTLibMaterials.Iron), WIRE_CUTTER.getToolItem(GTLibMaterials.Diamond), WIRE_CUTTER.getToolItem(GTLibMaterials.NetherizedDiamond)));
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerComponentProvider(EUProvider.INSTANCE, TooltipPosition.BODY, Block.class);
        registration.registerComponentProvider(MachineProvider.INSTANCE, TooltipPosition.BODY, Block.class);
    }
}
