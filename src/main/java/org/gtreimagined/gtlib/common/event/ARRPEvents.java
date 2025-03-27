package org.gtreimagined.gtlib.common.event;

import org.gtreimagined.gtlib.datagen.AntimatterDynamics;
import net.devtech.arrp.api.RRPEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ARRPEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onResourcePackAfterVanilla(RRPEvent.AfterVanilla event){
        AntimatterDynamics.addResourcePacks(event::addPack);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onResourcePackBeforeUser(RRPEvent.BetweenModsAndUser event){
        AntimatterDynamics.addDataPacks(event::addPack);
    }
}
