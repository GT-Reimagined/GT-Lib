package org.gtreimagined.gtlib.common.event;

import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.datagen.GTLibDynamics;
import net.devtech.arrp.api.RRPInitEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ref.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCommonEvents {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRRPInit(RRPInitEvent event){
        GTLibDynamics.runAssetProvidersDynamically();
    }


}
