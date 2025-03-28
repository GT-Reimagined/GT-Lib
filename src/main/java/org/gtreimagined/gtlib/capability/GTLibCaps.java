package org.gtreimagined.gtlib.capability;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.gtreimagined.gtlib.Ref;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ref.ID)
public class GTLibCaps {
    public static final BiMap<Class<?>, Capability<?>> CAP_MAP = HashBiMap.create();


    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent ev) {
    }
}
