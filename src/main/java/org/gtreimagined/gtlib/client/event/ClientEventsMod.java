package org.gtreimagined.gtlib.client.event;

import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.client.GTTextureStitcher;
import org.gtreimagined.gtlib.client.model.loader.IGTModelLoader;
import org.gtreimagined.gtlib.proxy.ClientHandler;
import org.gtreimagined.gtlib.registration.RegistrationEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ref.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEventsMod {
    @SubscribeEvent
    public static void onTextureStitch(final TextureStitchEvent.Pre event) {
        GTTextureStitcher.onTextureStitch(event.getAtlas(), event::addSprite);
    }

    @SubscribeEvent
    public static void onBlockColorHandler(RegisterColorHandlersEvent.Block e) {
        ClientHandler.onBlockColorHandler(e.getBlockColors());
    }

    @SubscribeEvent
    public static void onItemColorHandler(RegisterColorHandlersEvent.Item e) {
        ClientHandler.onItemColorHandler(e.getItemColors());
    }

    @SubscribeEvent
    public static void preResourceRegistration(ModelEvent.RegisterGeometryLoaders ev) {
        GTAPI.onRegistration(RegistrationEvent.CLIENT_DATA_INIT);
        GTAPI.all(IGTModelLoader.class).forEach(l -> {
            String domain = l.getDomain();
            ModContainer previous = ModLoadingContext.get().getActiveContainer();
            ModContainer newContainer = ModList.get().getModContainerById(domain).orElse(null);
            if (newContainer != null){
                if (!domain.equals(Ref.ID)){
                    ModLoadingContext.get().setActiveContainer(newContainer);
                }
            }
            ev.register(l.getId(), l);
            if (newContainer != null){
                if (!domain.equals(Ref.ID)){
                    ModLoadingContext.get().setActiveContainer(previous);
                }
            }
        });
    }
}
