package org.gtreimagined.gtlib.common.event;

import org.gtreimagined.gtlib.datagen.GTLibDynamics;
import net.devtech.arrp.api.RRPEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ARRPEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onResourcePackAfterVanilla(RRPEvent.AfterVanilla event){
        GTLibDynamics.addResourcePacks(event::addPack);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onResourcePackBeforeUser(RRPEvent.BetweenModsAndUser event){
        GTLibDynamics.addDataPacks(event::addPack);
    }
}
