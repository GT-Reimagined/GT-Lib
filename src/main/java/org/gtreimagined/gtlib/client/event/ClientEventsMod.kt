package org.gtreimagined.gtlib.client.event

import net.minecraftforge.client.event.ModelEvent
import net.minecraftforge.client.event.RegisterColorHandlersEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.ModList
import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.Ref
import org.gtreimagined.gtlib.client.model.loader.IGTModelLoader
import org.gtreimagined.gtlib.proxy.ClientHandler
import org.gtreimagined.gtlib.registration.RegistrationEvent
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT
import java.util.function.Consumer

object ClientEventsMod {
    /*@SubscribeEvent
    public static void onTextureStitch(final TextureStitchEvent.Pre event) {
        GTTextureStitcher.onTextureStitch(event.getAtlas(), event::addSprite);
    }*/
    @SubscribeEvent
    fun onBlockColorHandler(e: RegisterColorHandlersEvent.Block) {
        ClientHandler.onBlockColorHandler(e.blockColors)
    }

    @SubscribeEvent
    fun onItemColorHandler(e: RegisterColorHandlersEvent.Item) {
        ClientHandler.onItemColorHandler(e.itemColors)
    }

    @Suppress("removal", "DEPRECATION")
    @SubscribeEvent
    fun preResourceRegistration(ev: ModelEvent.RegisterGeometryLoaders) {
        GTAPI.onRegistration(RegistrationEvent.CLIENT_DATA_INIT)
        GTAPI.all(IGTModelLoader::class.java).forEach(Consumer { l: IGTModelLoader<*>? ->
            val domain = l!!.domain
            val previous = LOADING_CONTEXT.activeContainer
            val newContainer: ModContainer? = ModList.get().getModContainerById(domain).orElse(null)
            if (newContainer != null) {
                if (domain != Ref.ID) {
                    LOADING_CONTEXT.setActiveContainer(newContainer)
                }
            }
            ev.register(l.getId(), l)
            if (newContainer != null) {
                if (domain != Ref.ID) {
                    LOADING_CONTEXT.setActiveContainer(previous)
                }
            }
        })
    }
}
