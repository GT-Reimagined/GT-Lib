package org.gtreimagined.gtlib.integration;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.integration.top.TheOneProbePlugin;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

import static org.gtreimagined.gtlib.GTLib.LOGGER;

public class Integrations {
    public static void enqueueIMC(final InterModEnqueueEvent event) {
        if(GTAPI.isModLoaded(Ref.MOD_TOP)) {
            LOGGER.info("The One Probe is loaded, enabling integration");
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", TheOneProbePlugin::new);
        }
    }
}
