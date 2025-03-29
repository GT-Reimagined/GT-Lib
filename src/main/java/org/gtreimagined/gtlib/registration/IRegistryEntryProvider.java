package org.gtreimagined.gtlib.registration;

import net.minecraftforge.registries.IForgeRegistry;

public interface IRegistryEntryProvider extends IGTObject {

    void onRegistryBuild(IForgeRegistry<?> registry);
}
