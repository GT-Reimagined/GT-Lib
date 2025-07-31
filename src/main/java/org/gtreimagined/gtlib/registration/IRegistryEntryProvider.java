package org.gtreimagined.gtlib.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.IForgeRegistry;

public interface IRegistryEntryProvider extends IGTObject {

    void onRegistryBuild(ResourceKey<? extends Registry<?>> registry);
}
