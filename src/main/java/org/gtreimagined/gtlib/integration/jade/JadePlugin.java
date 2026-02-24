package org.gtreimagined.gtlib.integration.jade;

import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.data.GTLibMaterials;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.addon.harvest.HarvestToolProvider;
import snownee.jade.addon.harvest.SimpleToolHandler;
import org.gtreimagined.tesseract.Tesseract;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import static org.gtreimagined.gtlib.data.GTTools.WIRE_CUTTER;
import static org.gtreimagined.gtlib.data.GTTools.WRENCH;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(EUProvider.INSTANCE, BlockEntity.class);
        registration.registerBlockDataProvider(MachineProvider.INSTANCE, BlockEntityMachine.class);
        if (Data.getMaterialTiermap().containsKey(2) && Data.getMaterialTiermap().containsKey(3) && Data.getMaterialTiermap().containsKey(4)) {
            HarvestToolProvider.registerHandler(new SimpleToolHandler("wrench", WRENCH.getToolType(), WRENCH.getToolItem(Data.getMaterialTiermap().get(2)), WRENCH.getToolItem(Data.getMaterialTiermap().get(3)), WRENCH.getToolItem(Data.getMaterialTiermap().get(4))));
            HarvestToolProvider.registerHandler(new SimpleToolHandler("wire_cutter", WIRE_CUTTER.getToolType(), WIRE_CUTTER.getToolItem(Data.getMaterialTiermap().get(2)), WIRE_CUTTER.getToolItem(Data.getMaterialTiermap().get(3)), WIRE_CUTTER.getToolItem(Data.getMaterialTiermap().get(4))));
        }
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(EUProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(MachineProvider.INSTANCE, Block.class);
    }
}
