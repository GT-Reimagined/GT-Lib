package muramasa.antimatter.common.event;

import muramasa.antimatter.Ref;
import muramasa.antimatter.datagen.AntimatterDynamics;
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
        AntimatterDynamics.runAssetProvidersDynamically();
    }


}
