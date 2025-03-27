package org.gtreimagined.gtlib.integration;

import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.integration.top.TheOneProbePlugin;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

import static org.gtreimagined.gtlib.Antimatter.LOGGER;

public class Integrations {
    public static void enqueueIMC(final InterModEnqueueEvent event) {
        if(AntimatterAPI.isModLoaded(Ref.MOD_TOP)) {
            LOGGER.info("The One Probe is loaded, enabling integration");
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", TheOneProbePlugin::new);
        }
    }
}
